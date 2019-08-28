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
		String diffCaseFilePath = args[1];

		File originFile = new File(originFilePath);
		File diffCaseFile = new File(diffCaseFilePath);

		Optional<Element> buttonOrigin = findElementById(originFile, TARGET_ELEMENT_ID);
		String buttonText = buttonOrigin.map(b -> b.childNode(0).attributes().asList().get(0).getValue()).get();
		Map<String, String> buttonAttr = new HashMap<>();
		buttonOrigin.ifPresent(element -> element.attributes().asList()
				.forEach(attr -> buttonAttr.put(attr.getKey(), attr.getValue())));

		StringBuilder cssQuery = new StringBuilder("a[class*=\"" + buttonAttr.get("class") + "\"]");
		Optional<Elements> optElements = findElementsByQuery(diffCaseFile, cssQuery.toString());
		if (optElements.isPresent()) {
			Elements elements = optElements.get();
			if (elements.size() > 1) {
				cssQuery.insert(cssQuery.length(), "[title*=\"" + buttonAttr.get("title") + "\"]");
				optElements = findElementsByQuery(diffCaseFile, cssQuery.toString());
				optElements.ifPresent(value -> System.out.println(buildPathToDiffCaseElement(value.get(0))));
			} else {
				System.out.println(buildPathToDiffCaseElement(elements.get(0)));
			}
		}
	}

	private static String buildPathToDiffCaseElement(Element element) {
		StringBuilder res = new StringBuilder();
		while (element.parent() != null) {
			res.insert(0, " > " + element.tag());
			element = element.parent();
		}
		return res.substring(3);
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
