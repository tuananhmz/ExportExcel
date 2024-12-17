package com.example.exportexcel.controller;

import com.example.exportexcel.service.InfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/file")
public class InfoController {
    @Autowired
    private InfoService infoService;

    @PostMapping(value = "/upload" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload file",
            description = "API này cho phép upload file kiểu multipart.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "File Excel để upload",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
    )
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Kiểm tra xem file có phải là file Excel không
            if (!file.getOriginalFilename().endsWith(".xls") && !file.getOriginalFilename().endsWith(".xlsx")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only Excel files are allowed.");
            }

            // Nếu là file Excel, tiếp tục xử lý
            infoService.saveDataFromExcel(file);
            return ResponseEntity.ok("File uploaded and data saved successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process the file: " + e.getMessage());
        }
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportToExcel() throws IOException {
        // Lấy dữ liệu Excel từ service
        byte[] excelData = infoService.exportToExcel();

        // Tạo headers cho response
        HttpHeaders headers = new HttpHeaders();

        // Thêm thông tin Content-Disposition để file được tải về dưới dạng file đính kèm
        headers.add("Content-Disposition", "attachment; filename=HR_members_info.xlsx");

        // Thêm Content-Type để trình duyệt nhận diện đúng loại file
        headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        // Trả về response với dữ liệu và headers
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

}
