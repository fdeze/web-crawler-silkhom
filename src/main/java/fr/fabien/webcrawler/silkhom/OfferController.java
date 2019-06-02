package fr.fabien.webcrawler.silkhom;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.fabien.contracts.OfferVo;
import fr.fabien.webcrawler.silkhom.internal.SilkhomOfferService;

@EnableDiscoveryClient
@RestController
public class OfferController {
	private Logger logger = LoggerFactory.getLogger(OfferController.class);

	@Autowired
	private SilkhomOfferService silkhomProxy;

	@GetMapping(path = "/getOffers/silkhom", produces = { "application/json" })
	public List<OfferVo> getOffers() {

		logger.info("Reception requête vers silkhomProxy-microservice - getOffers");
		List<OfferVo> lOfferList = silkhomProxy.getOffers();
		logger.info("Reception requête vers silkhomProxy-microservice - getOffers - nombre résultats : {}",
				lOfferList.size());
		return lOfferList;
	}

}
