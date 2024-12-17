package com.example.exportexcel.service;

import com.example.exportexcel.entity.Info;
import com.example.exportexcel.repository.InfoRepository;
import jakarta.persistence.Column;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpHeaders;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InfoService {
    @Autowired
    private InfoRepository infoRepository;

    public void saveDataFromExcel(MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        // Đọc hàng tiêu đề
        Row headerRow = sheet.getRow(4);
        Map<String, Integer> columnIndexMap = new HashMap<>();
        for (Cell cell : headerRow) {
            if (cell.getCellStyle().getHidden()) continue; // Bỏ qua các cột ẩn
            columnIndexMap.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
        }

        // Duyệt từng hàng và lưu dữ liệu
        for (Row row : sheet) {
            if (row.getRowNum() == 0||row.getRowNum() == 1||row.getRowNum() == 2||row.getRowNum() == 3||row.getRowNum() == 4) continue; // Bỏ qua hàng tiêu đề

            // Kiểm tra nếu tất cả các ô trong dòng đều trống
            boolean isRowEmpty = true;  // Giả định dòng trống
            for (int colIndex = 0; colIndex < headerRow.getLastCellNum(); colIndex++) {
                String cellValue = getCellValue(row, colIndex);  // Giả sử getCellValue() trả về chuỗi
                Cell cell = row.getCell(colIndex);  // Lấy đối tượng Cell

                // Kiểm tra xem ô có bị ẩn không
                if (cell != null && !cell.getSheet().isColumnHidden(colIndex)) {
                    // Kiểm tra nếu ô không trống và không bị ẩn
                    if (cellValue != null && !cellValue.trim().isEmpty()) {
                        isRowEmpty = false;  // Nếu có ít nhất một ô có dữ liệu, dòng không trống
                        break;
                    }
                }
            }

            // Nếu dòng trống, dừng xử lý
            if (isRowEmpty) {
                break;  // Dừng vòng lặp nếu dòng trống
            }

            Info data = Info.builder()
                    .name(getCellValue(row, columnIndexMap.get("이름")))
                    .email(getCellValue(row, columnIndexMap.get("이메일")))
                    .employmentType(getCellValue(row, columnIndexMap.get("고용 형태")))
                    .status(getCellValue(row, columnIndexMap.get("상태")))
                    .joinDate(getCellValue(row, columnIndexMap.get("입사일")))
                    .registeredDepartment(getCellValue(row, columnIndexMap.get("기 등록 부서\n(변경 불가)")))
                    .newDepartment(getCellValue(row, columnIndexMap.get("신규 부서 할당\n(1개 부서만 할당 가능합니다.)")))
                    .position1(getCellValue(row, columnIndexMap.get("직위")))
                    .position2(getCellValue(row, columnIndexMap.get("직책")))
                    .job(getCellValue(row, columnIndexMap.get("직무\n(1개 이상의 경우 , 를 넣어 구분해주세요.)")))
                    .employeeNumber(getCellValue(row, columnIndexMap.get("사번")))
                    .detailWork(getCellValue(row, columnIndexMap.get("상세 업무")))
                    .build();
            infoRepository.save(data);
        }

        workbook.close();
    }

    private String getCellValue(Row row, Integer columnIndex) {
        if (columnIndex == null || row.getCell(columnIndex) == null) return null;

        Cell cell = row.getCell(columnIndex);
        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toString();
                    } else {
                        return String.valueOf(cell.getNumericCellValue());
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    return cell.getCellFormula();
                default:
                    return "";
            }
        } catch (Exception e) {
            return ""; // Trả về chuỗi rỗng nếu gặp lỗi
        }
    }
    public byte[] exportToExcel() throws IOException {
        // Fetch data from the database
        List<Info> infoList = infoRepository.findAll();

        // Create a new workbook
        Workbook workbook = new XSSFWorkbook();

        // Create sheet 1: HR_members_info
        Sheet sheet1 = workbook.createSheet("HR_members_info");

        // Create sheet 2: Dropdown source
        Sheet sheet2 = workbook.createSheet("Dropdown_source");
        // Add description at the top of sheet 2
        String[] descriptionSheet2 = {
                "Docswave 조직 정보",
                "Docswave에 등록된 조직 정보입니다. 엑셀 입력 시 범례로 사용되며\r\n해당 시트의 값을 수정 시, 조직원 정보 등록이 불가합니다.\r\n* 해당 시트의 내용 수정을 원하시면, Docswave 웹 서비스 내에서 개별적으로 수정 후 다시 Excel을 다운받아 등록해주세요.",
        };
        addDescription(sheet2, descriptionSheet2);

        // Define data for dropdowns
        String[] employmentTypes = {"정규직", "인턴", "머리부터발끝까지사랑스러워", "고용형태고용형태고용형태고용형태고용형태고용형태고용형태고용형태고용"};
        String[] departments = {"Docswave PX팀2", "Docswave PX팀1", "법무 1팀", "또하위부서일경우", "하위하위", "최하위", "법무 2팀", "법무 3팀", "행정부", "세무/회계팀", "광고/마케팅팀", "송무부", "조직원 없음", "ㄴㅇㄹ", "법무법인한바다"};
        String[] positions = {"직위엄청길어직위엄청길어직위엄청길어직위엄청길어직위엄청길어직위엄청길어직위엄청길어직위엄청길어직위", "대표이사", "재무이사", "운영이사", "기술이사", "전무이사", "부장", "차장", "과장", "대리", "직위"};
        String[] roles = {"본부장", "부장", "팀장", "파트장", "팀원", "직책엄청길어직책엄청길어직책엄청길어직책엄청길어직책엄청길어직책엄청길어직책엄청길어직책엄청길어직책", "직책"};
        String[] statuses = {"재직", "휴직", "퇴직"};
        String[] headersSheet2 = {
                "고용 형태", "등록 부서", "직위", "직책", "상태"
        };

        // Fill data in sheet 2 (dropdown source) vertically
        createRowWithValues(sheet2, 2, headersSheet2);

        int maxRows = Math.max(
                Math.max(employmentTypes.length, departments.length),
                Math.max(positions.length, Math.max(roles.length, statuses.length))
        );

        for (int i = 0; i < maxRows; i++) {
            Row row = sheet2.getRow(3 + i); // Bắt đầu từ hàng thứ 3
            if (row == null) {
                row = sheet2.createRow(3 + i); // Tạo hàng mới nếu chưa tồn tại
            }
            if (i < employmentTypes.length) {
                row.createCell(0).setCellValue(employmentTypes[i]); // Cột "고용 형태"
            }
            if (i < departments.length) {
                row.createCell(1).setCellValue(departments[i]); // Cột "등록 부서"
            }
            if (i < positions.length) {
                row.createCell(2).setCellValue(positions[i]); // Cột "직위"
            }
            if (i < roles.length) {
                row.createCell(3).setCellValue(roles[i]); // Cột "직책"
            }
            if (i < statuses.length) {
                row.createCell(4).setCellValue(statuses[i]); // Cột "상태"
            }
        }

       // Adjust column widths to fit the data
       formatWrapText(sheet2,headersSheet2,workbook);
        // Hide sheet 2 (optional)
        // workbook.setSheetHidden(workbook.getSheetIndex(sheet2), true);

        // Add description text at the top of sheet 1
        String[] description = {
                "1. Docswave 조직원 정보 - 기본 / 인사 정보",
                "Docswave에 생성된 조직에 함께 이용할 조직원 정보를 쉽게 등록할 수 있습니다.\r\n기존 조직원의 정보를 변경하게 될 경우 업데이트되며, (이메일 정보 기준)\r\n신규 등록 또는 다른 이메일로 등록 시 신규 생성됩니다.\n* 최초 등록 시 조직원의 상태는 \"재직\" 중만 가능하며, 변경하고자 하시면 \"관리자 > 인사 관리 > 조직도 / 조직원 관리\"에서 변경 가능합니다.\r\n* 조직원의 신규 부서 할당은 최대 1개까지만 가능하며, 추가 등록을 원하시면 Docswave 웹 화면 내에서 등록해주세요.\r\n* 엑셀을 통해 등록 시 관리자로는 등록이 불가합니다. 조직원 등록 이후 \"관리자 > 권한 관리\"에서 추가할 관리자를 선택해주세요.",
        };

        int descriptionRowIdx = addDescription(sheet1, description);

        // Add parent headers
        int parentHeaderRowIdx = descriptionRowIdx;
        Row parentHeaderRow = sheet1.createRow(parentHeaderRowIdx);
        parentHeaderRow.createCell(0).setCellValue("필수 입력 정보");
        parentHeaderRow.createCell(6).setCellValue("선택 입력 정보");

        // Merge parent header cells
        sheet1.addMergedRegion(new CellRangeAddress(parentHeaderRowIdx, parentHeaderRowIdx, 0, 5));
        sheet1.addMergedRegion(new CellRangeAddress(parentHeaderRowIdx, parentHeaderRowIdx, 6, 11));

        // Define headers based on the Excel template
        String[] headers = {
                "이름", "이메일", "고용 형태", "상태", "입사일", "기 등록 부서", "신규 부서 할당",
                "직위", "직책", "직무", "사번", "상세 업무"
        };

        // Create header row in sheet 1
        createHeaderRow(sheet1, headers, parentHeaderRowIdx + 1);

        // Add dropdowns in sheet 1 using the correct range in Sheet2
        XSSFDataValidationHelper validationHelper = new XSSFDataValidationHelper((XSSFSheet) sheet1);
        int startRow = parentHeaderRowIdx + 2; // Dòng đầu tiên
        int endRow = parentHeaderRowIdx + infoList.size()+1; // Dòng cuối cùng

        addDropdownToSheet(sheet1, validationHelper, "Dropdown_source!$A$4:$A$" + (employmentTypes.length + 3), // Employment Types
                new CellRangeAddress(startRow, endRow, 2, 2));  // Adjust the range

        addDropdownToSheet(sheet1, validationHelper, "Dropdown_source!$B$4:$B$" + (departments.length + 3), // Departments
                new CellRangeAddress(startRow, endRow, 6, 6));  // Adjust the range

        addDropdownToSheet(sheet1, validationHelper, "Dropdown_source!$C$4:$C$" + (positions.length + 3), // Positions
                new CellRangeAddress(startRow, endRow, 7, 7));  // Adjust the range

        addDropdownToSheet(sheet1, validationHelper, "Dropdown_source!$D$4:$D$" + (roles.length + 3), // Roles
                new CellRangeAddress(startRow, endRow, 8, 8));  // Adjust the range

        addDropdownToSheet(sheet1, validationHelper, "Dropdown_source!$E$4:$E$" + (statuses.length + 3), // Statuses
                new CellRangeAddress(startRow, endRow, 3, 3));  // Adjust the range

        // Fill data rows in sheet 1
        int rowIdxSheet1 = parentHeaderRowIdx + 2;
        for (Info info : infoList) {
            Row row = sheet1.createRow(rowIdxSheet1++);
            row.createCell(0).setCellValue(info.getName());
            row.createCell(1).setCellValue(info.getEmail());
            row.createCell(2).setCellValue(info.getEmploymentType());
            row.createCell(3).setCellValue(info.getStatus());
            row.createCell(4).setCellValue(info.getJoinDate());
            row.createCell(5).setCellValue(info.getRegisteredDepartment());
            row.createCell(6).setCellValue(info.getNewDepartment());
            row.createCell(7).setCellValue(info.getPosition1());
            row.createCell(8).setCellValue(info.getPosition2());
            row.createCell(9).setCellValue(info.getJob());
            row.createCell(10).setCellValue(info.getEmployeeNumber());
            row.createCell(11).setCellValue(info.getDetailWork());
        }

        // Auto-size columns in sheet 1

        formatWrapText(sheet1,headers,workbook);

        // Write to byte array output stream
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();
        }
    }


    private void createRowWithValues(Sheet sheet, int rowIndex, String[] values) {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i]);
        }
    }

    private int addDescription(Sheet sheet, String[] description) {
        int rowIndex = 0;
        for (String line : description) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(line);
        }
        return rowIndex;
    }

    private void createHeaderRow(Sheet sheet, String[] headers, int rowIndex) {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < headers.length; i++) {
            row.createCell(i).setCellValue(headers[i]);
        }
    }

    private void addDropdownToSheet(Sheet sheet, XSSFDataValidationHelper validationHelper, String formula, CellRangeAddress address) {
        // Tạo ràng buộc cho dropdown (danh sách từ công thức)
        XSSFDataValidationConstraint constraint = (XSSFDataValidationConstraint) validationHelper.createFormulaListConstraint(formula);

        // Chuyển đổi từ CellRangeAddress sang CellRangeAddressList
        CellRangeAddressList addressList = new CellRangeAddressList(address.getFirstRow(), address.getLastRow(), address.getFirstColumn(), address.getLastColumn());

        // Tạo validation sử dụng ràng buộc và danh sách phạm vi ô
        XSSFDataValidation validation = (XSSFDataValidation) validationHelper.createValidation(constraint, addressList);

        // Hiển thị hộp thông báo lỗi khi nhập dữ liệu sai
        validation.setShowErrorBox(true);

        // Thêm validation vào sheet
        sheet.addValidationData(validation);
    }

    private void formatWrapText(Sheet sheet, String[] headers, Workbook workbook) {
        // Tạo CellStyle chỉ có wrap text (dành cho 2 dòng đầu)
        CellStyle wrapTextOnlyStyle = workbook.createCellStyle();
        wrapTextOnlyStyle.setWrapText(true);

        // Tạo CellStyle có wrap text và căn giữa (dành cho các dòng còn lại)
        CellStyle wrapTextCenterStyle = workbook.createCellStyle();
        wrapTextCenterStyle.setWrapText(true);
        wrapTextCenterStyle.setAlignment(HorizontalAlignment.CENTER);
        wrapTextCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Áp dụng định dạng cho các dòng
        for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                for (int colIndex = 0; colIndex < headers.length; colIndex++) {
                    Cell cell = row.getCell(colIndex);
                    if (cell != null) {
                        // Áp dụng định dạng tùy theo dòng
                        if (rowIndex < 2) {
                            cell.setCellStyle(wrapTextOnlyStyle); // Chỉ wrap text cho dòng 1 và 2
                        } else {
                            cell.setCellStyle(wrapTextCenterStyle); // Wrap text và căn giữa từ dòng 3 trở đi
                        }
                    }
                }
            }
        }

        // Tự động điều chỉnh độ rộng cột và thêm không gian để phù hợp với wrap text
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i); // Auto-size để vừa dữ liệu
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, currentWidth + 512); // Tăng thêm một chút không gian (512 đơn vị)
        }
    }
}
