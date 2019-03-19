package fr.fabien.webcrawler.silkhom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.fabien.webcrawler.common.Constants;
import fr.fabien.webcrawler.common.DateParseUtils;
import fr.fabien.webcrawler.common.model.SilkhomOfferVo;

@EnableDiscoveryClient
@RestController
public class OfferController {

	private static String URL = "https://www.silkhom.com/toutes-nos-offres-demploi/";

	@GetMapping(path = "/getOffers/silkhom", produces = { "application/json" })
	public List<SilkhomOfferVo> getOffers() {
		List<SilkhomOfferVo> lOffers = new ArrayList();
		try {

			Document lDocument = Jsoup.connect(URL).userAgent(Constants.USER_AGENT).get();

			SilkhomOfferVo lOffer;
			Elements lAElement;
			Elements metasOffer;
			Elements lOfferElements = lDocument.select("#gridOffers .locations-auvergne-rhone-alpes");

			Elements lFilteredOfferElements = new Elements(
					lOfferElements.parallelStream()
					.filter(offer -> offer.toString().contains("contrat-CDI"))
					.collect(Collectors.toList()));

			String lUrl;
			for (Element offerElement : lFilteredOfferElements) {
				lOffer = new SilkhomOfferVo();

				lAElement = offerElement.select(".single-offre-header a");

				lUrl = lAElement.attr("href");
				lOffer.setNumeroOffreExterne("SILKHOM_" + lUrl.hashCode());
				lOffer.setUrl(lUrl);
				lOffer.setTitre(lAElement.text());

				metasOffer = offerElement.select(".single-offre-body .offre-infos span");
				for (Element meta : metasOffer) {
					String lStrMeta = meta.select("span").text();
					if (DateParseUtils.isDate(lStrMeta)) {
						lOffer.setDatePublication(lStrMeta);
					} else {
						lOffer.getMetas().add(lStrMeta);
					}

				}

				metasOffer = offerElement.select(".offre-resume p");
				if (metasOffer != null && !metasOffer.isEmpty()) {
					List<String> lListMetas = Arrays.asList(metasOffer.get(0).text().split("-"));

					lListMetas.parallelStream().filter(pair -> !pair.isEmpty())
							.forEachOrdered(lOffer.getInformations()::add);

				}
				lOffers.add(lOffer);

			}
			return lOffers;
		} catch (IOException e) {
			return lOffers;
		}

	}

}
