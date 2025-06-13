package backend.service;

import backend.service.CaptchaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CaptchaService {

    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;

    private static final String GOOGLE_RECAPTCHA_VERIFY_URL =
            "https://www.google.com/recaptcha/api/siteverify";

    public boolean verifyCaptcha(String token) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> params = new HashMap<>();
        params.put("secret", recaptchaSecret);
        params.put("response", token);

        CaptchaResponse apiResponse = restTemplate.postForObject(
                GOOGLE_RECAPTCHA_VERIFY_URL + "?secret={secret}&response={response}",
                null,
                CaptchaResponse.class,
                params
        );

        return apiResponse != null && apiResponse.isSuccess();
    }
}
