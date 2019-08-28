package com.test_task;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class App {

	private static Logger LOGGER = LoggerFactory.getLogger(App.class);

	private static String CHARSET_NAME = "utf8";
	private static String TARGET_ELEMENT_ID = "make-everything-ok-button";

	public static void main(String[] args) {
		String originFilePath = args[0];
//		String originFilePath = "C:\\Users\\Pazynych\\Downloads\\startbootstrap-sb-admin-2-examples\\sample-0-origin.html";
		String diffCaseFilePath = args[1];
//		String diffCaseFilePath = "C:\\Users\\Pazynych\\Downloads\\startbootstrap-sb-admin-2-examples\\sample-1-evil-gemini.html";

		File originFile = new File(originFilePath);
		File diffCaseFile = new File(diffCaseFilePath);

		Optional<Element> buttonOptOrigin = findElementById(originFile, TARGET_ELEMENT_ID);
		String buttonText = buttonOptOrigin.map(b -> b.childNode(0).attributes().asList().get(0).getValue()).get();
		Map<String, String> buttonAttr = new HashMap<>();
		buttonOptOrigin.ifPresent(element -> element.attributes().asList()
				.forEach(attr -> buttonAttr.put(attr.getKey(), attr.getValue())));

		StringBuilder cssQuery0 = new StringBuilder("a[class*=\"" + buttonAttr.get("class") + "\"]");
//		StringBuilder cssQuery1 = new StringBuilder("a[title*=\"" + buttonAttr.get("title") + "\"]");
		Optional<Elements> optElementsWithCorrectClass = findElementsByQuery(diffCaseFile, cssQuery0.toString());
		List<Element> elementsWithCorrectClass = optElementsWithCorrectClass.map(ArrayList::new).orElse(null);

		List<Element> collect = elementsWithCorrectClass.stream()
				.filter(element -> element.childNode(0).attributes().asList().get(0).getValue().equals(buttonText))
				.collect(Collectors.toList());
		Element element = collect.get(0);

		System.out.println(buildPathToDiffCaseElement(element).substring(2));
	}

	private static StringBuilder buildPathToDiffCaseElement(Element element) {
		StringBuilder res = new StringBuilder();
		while(element.parent() != null) {
			res.insert(0, " > " + element.tag());
			element = element.parent();
		}
		return res;
	}

	private static Optional<Element> findElementById(File htmlFile, String targetElementId) {
		try {
			Document doc = Jsoup.parse(
					htmlFile,
					CHARSET_NAME,
					htmlFile.getAbsolutePath());

			return Optional.of(doc.getElementById(targetElementId));

		} catch (IOException e) {
			LOGGER.error("Error reading [{}] file", htmlFile.getAbsolutePath(), e);
			return Optional.empty();
		}
	}

	private static Optional<Elements> findElementsByQuery(File htmlFile, String cssQuery) {
		try {
			Document doc = Jsoup.parse(
					htmlFile,
					CHARSET_NAME,
					htmlFile.getAbsolutePath());

			return Optional.of(doc.select(cssQuery));

		} catch (IOException e) {
			LOGGER.error("Error reading [{}] file", htmlFile.getAbsolutePath(), e);
			return Optional.empty();
		}
	}

}
