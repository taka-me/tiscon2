package net.unit8.sigcolle.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Digits;

/**
 * キャンペーン新規作成Formクラス.
 *
 * @author Yoshitaka Honda
 */
@Data
public class CampaignCreateForm extends FormBase {
    /**
     * タイトル
     */
    @NotBlank
    @Length(max = 30)
    private String title;
    /**
     * 本文
     */
    @NotBlank
    @Length(max = 3000)
    private String statement;
    /**
     * 達成人数
     */
    @NotBlank
    @Digits(integer = 10, fraction = 0)
    private String goal;


}
