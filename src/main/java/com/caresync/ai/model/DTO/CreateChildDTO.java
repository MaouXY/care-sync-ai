package com.caresync.ai.model.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 创建儿童账号请求参数
 */
@Data
@Schema(description = "创建儿童账号请求参数")
public class CreateChildDTO {
    @Schema(description = "儿童登录ID", example = "CHILD2024003")
    @NotBlank(message = "儿童登录ID不能为空")
    @Pattern(regexp = "^[A-Z]+[0-9]+$", message = "儿童登录ID格式不正确，应为字母+数字组合")
    private String childNo;

    @Schema(description = "儿童姓名", example = "张三")
    @NotBlank(message = "儿童姓名不能为空")
    @Size(max = 50, message = "儿童姓名长度不能超过50个字符")
    private String name;

    @Schema(description = "儿童年龄", example = "10")
    @NotNull(message = "儿童年龄不能为空")
    private Integer age;

    @Schema(description = "性别", example = "男")
    @NotBlank(message = "性别不能为空")
    @Pattern(regexp = "^(男|女)$", message = "性别只能是'男'或'女'")
    private String gender;

    @Schema(description = "出生日期", example = "2014-08-20")
    private LocalDate birthDate;

    @Schema(description = "身份证号", example = "110101201408205678")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$", message = "身份证号格式不正确")
    private String idCard;

    @Schema(description = "家庭地址", example = "北京市朝阳区建国路88号")
    @Size(max = 200, message = "家庭地址长度不能超过200个字符")
    private String address;

    @Schema(description = "联系电话", example = "13711114444")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "监护人姓名", example = "张父")
    @NotBlank(message = "监护人姓名不能为空")
    @Size(max = 50, message = "监护人姓名长度不能超过50个字符")
    private String guardianName;

    @Schema(description = "监护人电话", example = "13611115555")
    @NotBlank(message = "监护人电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "监护人手机号格式不正确")
    private String guardianPhone;

    @Schema(description = "4位登录验证码", example = "1234")
    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 4, message = "验证码必须是4位")
    @Pattern(regexp = "^[0-9]{4}$", message = "验证码必须是4位数字")
    private String verifyCode;

    @Schema(description = "备注信息", example = "性格内向，需要情感陪伴")
    @Size(max = 500, message = "备注信息长度不能超过500个字符")
    private String notes;
}