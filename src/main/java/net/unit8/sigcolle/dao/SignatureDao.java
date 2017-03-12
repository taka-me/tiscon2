package net.unit8.sigcolle.dao;

import java.util.List;

import net.unit8.sigcolle.DomaConfig;
import net.unit8.sigcolle.model.Signature;
import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;

/**
 * @author kawasima
 */
@Dao(config = DomaConfig.class)
public interface SignatureDao {
    @Select
    int countByCampaignId(Long campaignId);

    @Select
    List<Signature> selectAllByCampaignId(Long campaignId);

    @Select
    int countByName(String name, long camId);

    @Insert
    int insert(Signature signature);
}
