package fr.fabien.webcrawler.silkhom.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.fabien.contracts.silkhom.SilkhomOfferVo;
import fr.fabien.webcrawler.common.Constants;
import fr.fabien.webcrawler.common.DateParseUtils;

@Service
public class SilkhomOfferServiceImpl implements SilkhomOfferService {

	private Logger logger = LoggerFactory.getLogger(SilkhomOfferServiceImpl.class);

	private static String URL = "https://www.silkhom.com/toutes-nos-offres-demploi/";

	public List<SilkhomOfferVo> getOffers() {

		List<SilkhomOfferVo> lOffers = new ArrayList<>();
		try {

			Document lDocument = Jsoup.connect(URL).userAgent(Constants.USER_AGENT).get();

			Elements lOfferElements = lDocument.select("#gridOffers .locations-auvergne-rhone-alpes");
			Elements lFilteredOfferElements = new Elements(lOfferElements.parallelStream()
					.filter(offer -> offer.toString().contains("contrat-CDI")).collect(Collectors.toList()));

			return lFilteredOfferElements.parallelStream().map(element -> getDetailOffer(element))
					.collect(Collectors.toList());

		} catch (IOException e) {
			logger.error("getOffers - IOException", e);
			return lOffers;
		}

	}

	private SilkhomOfferVo getDetailOffer(Element offerElement) {
		SilkhomOfferVo lOffer = new SilkhomOfferVo();

		try {
			Elements lAElement = offerElement.select(".single-offre-header a");

			String lUrl = lAElement.attr("href");
			lOffer.setNumeroOffreExterne("SILKHOM_" + lUrl.hashCode());
			lOffer.setNumeroOffre(Integer.valueOf(lUrl.hashCode()).toString());

			lOffer.setUrl(lUrl);
			lOffer.setTitre(lAElement.text());

			Elements metasOffer = offerElement.select(".single-offre-body .offre-infos span");
			if (null != metasOffer) {
				for (Element meta : metasOffer) {
					String lStrMeta = meta.select("span").text();
					if (DateParseUtils.isDate(lStrMeta)) {
						lOffer.setDatePublication(lStrMeta);
					} else if (!"CDI".equals(lStrMeta)) {
						lOffer.setAdresse(lStrMeta);
					}
				}
			}

			metasOffer = offerElement.select(".offre-resume p");
			if (metasOffer != null && !metasOffer.isEmpty()) {
				List<String> lListMetas = Arrays.asList(metasOffer.get(0).text().split("-"));
				lOffer.setInformations(lListMetas.toString().replaceAll("\\[, ", "").replaceAll("]", ""));

			}

			Document lDocumentOffre = Jsoup.connect(lUrl).userAgent(Constants.USER_AGENT).get();
			Elements descriptionElement = lDocumentOffre.select(".single-page");

			String descriptionOffreTotal = descriptionElement.html();

			String descriptionEntreprise = StringUtils.substringBetween(descriptionOffreTotal,
					"<h2 class=\"mt-0\">Société</h2>", "<h2>Poste</h2>");
			lOffer.setEntreprise(descriptionEntreprise);

			String descriptionOffre = StringUtils.substringBetween(descriptionOffreTotal, "<h2>Poste</h2> ",
					"<h2>Profil recherché</h2>");
			lOffer.setDescriptionOffre(descriptionOffre);

			String descriptionProfil = StringUtils.substringBetween(descriptionOffreTotal, "<h2>Profil recherché</h2>",
					"<h2>Compléments</h2>");
			lOffer.setDescriptionProfil(descriptionProfil);

			String adresse = StringUtils.substringBetween(descriptionOffreTotal, "Lieu :", "<br>");
			lOffer.setAdresse(adresse);

			String salaire = StringUtils.substringBetween(descriptionOffreTotal, "Salaire :", "</p> ");
			lOffer.setSalaire(salaire);

		} catch (IOException exception) {
			Thread.currentThread().interrupt();
			logger.error("erreur", exception);
		}
		return lOffer;
	}

	/*
	 * public List<SilkhomOfferVo> getOffers() {
	 * 
	 * List<SilkhomOfferVo> lOffers = new ArrayList<>(); try {
	 * 
	 * Document lDocument =
	 * Jsoup.connect(URL).userAgent(Constants.USER_AGENT).get();
	 * 
	 * Elements lOfferElements =
	 * lDocument.select("#gridOffers .locations-auvergne-rhone-alpes"); Elements
	 * lFilteredOfferElements = new Elements(lOfferElements.parallelStream()
	 * .filter(offer ->
	 * offer.toString().contains("contrat-CDI")).collect(Collectors.toList()));
	 * 
	 * String lUrl; SilkhomOfferVo lOffer; Elements lAElement; Elements metasOffer;
	 * for (Element offerElement : lFilteredOfferElements) { lOffer = new
	 * SilkhomOfferVo();
	 * 
	 * lAElement = offerElement.select(".single-offre-header a");
	 * 
	 * lUrl = lAElement.attr("href"); lOffer.setNumeroOffreExterne("SILKHOM_" +
	 * lUrl.hashCode());
	 * lOffer.setNumeroOffre(Integer.valueOf(lUrl.hashCode()).toString());
	 * 
	 * lOffer.setUrl(lUrl); lOffer.setTitre(lAElement.text());
	 * 
	 * metasOffer = offerElement.select(".single-offre-body .offre-infos span"); if
	 * (null != metasOffer) { for (Element meta : metasOffer) { String lStrMeta =
	 * meta.select("span").text(); if (DateParseUtils.isDate(lStrMeta)) {
	 * lOffer.setDatePublication(lStrMeta); } else if (!"CDI".equals(lStrMeta)) {
	 * lOffer.setAdresse(lStrMeta); } } }
	 * 
	 * metasOffer = offerElement.select(".offre-resume p"); if (metasOffer != null
	 * && !metasOffer.isEmpty()) { List<String> lListMetas =
	 * Arrays.asList(metasOffer.get(0).text().split("-"));
	 * lOffer.setInformations(lListMetas.toString().replaceAll("\\[, ",
	 * "").replaceAll("]", ""));
	 * 
	 * }
	 * 
	 * Document lDocumentOffre =
	 * Jsoup.connect(lUrl).userAgent(Constants.USER_AGENT).get(); Elements
	 * descriptionElement = lDocumentOffre.select(".single-page");
	 * 
	 * String descriptionOffreTotal = descriptionElement.html();
	 * 
	 * String descriptionEntreprise =
	 * StringUtils.substringBetween(descriptionOffreTotal,
	 * "<h2 class=\"mt-0\">Société</h2>", "<h2>Poste</h2>");
	 * lOffer.setEntreprise(descriptionEntreprise);
	 * 
	 * String descriptionOffre = StringUtils.substringBetween(descriptionOffreTotal,
	 * "<h2>Poste</h2> ", "<h2>Profil recherché</h2>");
	 * lOffer.setDescriptionOffre(descriptionOffre);
	 * 
	 * String descriptionProfil =
	 * StringUtils.substringBetween(descriptionOffreTotal,
	 * "<h2>Profil recherché</h2>", "<h2>Compléments</h2>");
	 * lOffer.setDescriptionProfil(descriptionProfil);
	 * 
	 * String adresse = StringUtils.substringBetween(descriptionOffreTotal,
	 * "Lieu :", "<br>"); lOffer.setAdresse(adresse);
	 * 
	 * String salaire = StringUtils.substringBetween(descriptionOffreTotal,
	 * "Salaire :", "</p> "); lOffer.setSalaire(salaire);
	 * 
	 * lOffers.add(lOffer); } return lOffers;
	 * 
	 * } catch (IOException e) { logger.error("getOffers - IOException", e); return
	 * lOffers; }
	 * 
	 * }
	 */

}