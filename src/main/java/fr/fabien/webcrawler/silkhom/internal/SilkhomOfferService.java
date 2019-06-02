package fr.fabien.webcrawler.silkhom.internal;

import java.util.List;

import fr.fabien.contracts.OfferVo;
import fr.fabien.contracts.silkhom.SilkhomOfferVo;

public interface SilkhomOfferService {

	public List<OfferVo> getOffers();
}
