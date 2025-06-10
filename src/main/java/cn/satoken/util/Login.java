package cn.satoken.util;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Login {
    //NotBlank在判断时会忽略前后空格
    @NotBlank
    @Size(min = 6, max = 16)
    private String username;
    @NotBlank
    @Size(min = 6, max = 16)
    private String password;
    @NotBlank
    private String captchaId;
    @NotBlank
    private String captcha;
    private LocalDateTime birthday;
}
