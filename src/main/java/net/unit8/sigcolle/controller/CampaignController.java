package net.unit8.sigcolle.controller;

import javax.inject.Inject;
import javax.transaction.Transactional;

import enkan.component.doma2.DomaProvider;
import enkan.data.Flash;
import enkan.data.HttpResponse;
import enkan.data.Session;
import kotowari.component.TemplateEngine;
import net.unit8.sigcolle.auth.LoginUserPrincipal;
import net.unit8.sigcolle.dao.CampaignDao;
import net.unit8.sigcolle.dao.SignatureDao;
import net.unit8.sigcolle.dao.UserDao;
import net.unit8.sigcolle.form.CampaignCreateForm;
import net.unit8.sigcolle.form.CampaignForm;
import net.unit8.sigcolle.form.SignatureForm;
import net.unit8.sigcolle.model.Campaign;
import net.unit8.sigcolle.model.Signature;
import net.unit8.sigcolle.model.User;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import static enkan.util.HttpResponseUtils.RedirectStatusCode.SEE_OTHER;
import static enkan.util.HttpResponseUtils.redirect;
import static enkan.util.ThreadingUtils.some;

/**
 * @author kawasima
 */
public class CampaignController {
    @Inject
    private TemplateEngine templateEngine;

    @Inject
    private DomaProvider domaProvider;

    /**
     * キャンペーン詳細画面表示.
     *
     * @param form  URLパラメータ
     * @param flash flash scope session
     * @return HttpResponse
     */
    public HttpResponse index(CampaignForm form, Flash flash) {
        if (form.hasErrors()) {
            HttpResponse response = HttpResponse.of("Invalid");
            response.setStatus(400);
            return response;
        }

        return showCampaign(form.getCampaignIdLong(),
                new SignatureForm(),
                (String) some(flash, Flash::getValue).orElse(null));
    }

    /**
     * 署名の追加処理.
     *
     * @param form 画面入力された署名情報.
     * @return HttpResponse
     */
    @Transactional
    public HttpResponse sign(SignatureForm form ,
                             Session session) {

        if(session==null){
            HttpResponse response = redirect("../../register", SEE_OTHER);
            return response;
        } else {
            if (form.hasErrors()) {
                return showCampaign(form.getCampaignIdLong(), form, null);
            }
            Signature signature = new Signature();
            signature.setCampaignId(form.getCampaignIdLong());
            signature.setName(form.getName());
            signature.setSignatureComment(form.getSignatureComment());

            SignatureDao signatureDao = domaProvider.getDao(SignatureDao.class);
            signatureDao.insert(signature);

            HttpResponse response = redirect("/campaign/" + form.getCampaignId(), SEE_OTHER);
            response.setFlash(new Flash<>("ご賛同ありがとうございました！"));
            return response;

        }

    }

    /**
     * 新規キャンペーン作成画面表示.
     *
     * @return HttpResponse
     */
    public HttpResponse newCampaign() {
        return templateEngine.render("campaign/new", "form", new CampaignCreateForm());
    }

    /**
     * 新規キャンペーンを作成します.
     * ---------------------------------------
     * FIXME このメソッドは作成途中です.
     *
     * @param form    入力フォーム
     * @param session ログインしているユーザsession
     */
    @Transactional
    public HttpResponse create(CampaignCreateForm form,
                               Session session) {
        if (form.hasErrors()) {
            return templateEngine.render("campaign/new", "form", form);
        }
        LoginUserPrincipal principal = (LoginUserPrincipal) session.get("principal");

        PegDownProcessor processor = new PegDownProcessor(Extensions.ALL);

        // TODO タイトル, 目標人数を登録する
        Campaign model = new Campaign();
        model.setStatement(processor.markdownToHtml(form.getStatement()));  //本文
        model.setCreateUserId(principal.getUserId());   //ユーザID
        model.setTitle(form.getTitle());   //タイトル
        model.setGoal(Long.parseLong(form.getGoal()));  //目標人数



        CampaignDao campaignDao = domaProvider.getDao(CampaignDao.class);
        // TODO Databaseに登録する
        campaignDao.insert(model);  //campaignDaoにmodelに入った情報を格納する
        /*
        if(campaignDao.countByTitle(form.getTitle())==1){
            // 同タイトルがすでにある場合
            return templateEngine.render("",
                    "user", form
            );
        }
        */



        HttpResponse response = redirect("/campaign/" + model.getCampaignId(), SEE_OTHER);
        response.setFlash(new Flash<>("キャンペーンが作成できたよ！！やったね！！"/* TODO: キャンペーンが新規作成できた旨のメッセージを生成する */));

        return response;
    }

    /**
     * ログインユーザの作成したキャンペーン一覧を表示します.
     * ---------------------------------------
     * FIXME このメソッドは作成途中です.
     *
     * @param session ログインしているユーザsession
     */
    public HttpResponse listCampaigns(Session session) {
        throw new UnsupportedOperationException("実装してください !!");
    }

    private HttpResponse showCampaign(Long campaignId,
                                      SignatureForm form,
                                      String message) {
        CampaignDao campaignDao = domaProvider.getDao(CampaignDao.class);
        Campaign campaign = campaignDao.selectById(campaignId);
        UserDao userDao = domaProvider.getDao(UserDao.class);
        User user = userDao.selectByUserId(campaign.getCreateUserId());

        SignatureDao signatureDao = domaProvider.getDao(SignatureDao.class);

        int signatureCount = signatureDao.countByCampaignId(campaignId); //賛同者数
        int result = Integer.parseInt(campaign.getGoal().toString()) - signatureCount; //目標数-賛同者数

        String s_result = String.valueOf(result); //resultをString型に変換

        String sign_message =""; //最終的に表示するメッセージを格納

        if(result <= 0) {
            //目標を達成できている場合
            sign_message = campaign.getGoal().toString() + "人の賛同者が集まり，目標を達成しました！！";
        } else {
            //目標を達成していない場合
            sign_message = campaign.getGoal().toString()+ "人まで残り" + s_result +"人の賛同者が必要です！";
        }

        return templateEngine.render("campaign/index",
                "campaign", campaign,
                "user", user,
                "signatureCount", signatureCount,
                "sign_message", sign_message,
                "signature", form,
                "message", message
        );
    }
}
