package com.test_task;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class App {

	private static Logger LOGGER = LoggerFactory.getLogger(App.class);

	private static String CHARSET_NAME = "utf8";

	public static void main(String[] args) {
		String originFilePath = args[0];
		String diffCaseFilePath = args[1];
		String targetElementId = args[2];

		File originFile = new File(originFilePath);
		File diffCaseFile = new File(diffCaseFilePath);
		Path outputPath = Paths.get("src/main/resources/comparisonOutputForSamplePages.txt");

		Optional<Element> originalButton = findElementById(originFile, targetElementId);
		if (originalButton.isPresent()) {
			String buttonText = originalButton.map(b -> b.childNode(0).attributes().asList().get(0).getValue()).get().trim();
			Optional<Elements> optElements = findElementsByQuery(diffCaseFile, "a:contains(" + buttonText + ")");
			optElements.ifPresent(elements -> {
				try {
					Files.write(outputPath,
							(getPathToDiffCaseElement(diffCaseFile, originalButton.get(), elements) + "\n").getBytes(),
							StandardOpenOption.APPEND);
				} catch (IOException e) {
					LOGGER.error("Error writing result to file", e);
				}
			});
		}
	}

	private static String getPathToDiffCaseElement(File diffCaseFile, Element originalButton, Elements elements) {
		if (elements.size() != 1) {
			List<Element> visibleElements = elements.stream()
					.filter(App::isElementVisible)
					.collect(Collectors.toList());
			if (visibleElements.size() != 1) {
				Map<String, String> buttonAttr = originalButton.attributes().asList().stream()
						.collect(Collectors.toMap(Attribute::getKey, Attribute::getValue));
				return findDiffCaseElementByCssQuery(diffCaseFile, buttonAttr, "class");
			}
		}
		return getPathToDiffCaseElement(elements.get(0));
	}

	private static boolean isElementVisible(Element element) {
		return !(element.attr("style").equals("display:none"));
	}

	private static String findDiffCaseElementByCssQuery(File diffCaseFile, Map<String, String> buttonAttr, String attrName) {
		StringBuilder cssQuery = new StringBuilder("a[" + attrName + "*=\"" + buttonAttr.get(attrName) + "\"]");
		Optional<Elements> optElements = findElementsByQuery(diffCaseFile, cssQuery.toString());
		if (optElements.isPresent()) {
			Elements elements = optElements.get();
			if (elements.size() == 0) {
				return findDiffCaseElementByCssQuery(diffCaseFile, buttonAttr, "title");
			} else if (elements.size() > 1) {
				cssQuery.insert(cssQuery.length(), "[title*=\"" + buttonAttr.get("title") + "\"]");
				optElements = findElementsByQuery(diffCaseFile, cssQuery.toString());
				if (optElements.isPresent()) {
					return getPathToDiffCaseElement(optElements.get().get(0));
				}
			} else {
				return getPathToDiffCaseElement(elements.get(0));
			}
		}
		return "Diff case not found";
	}

	private static String getPathToDiffCaseElement(Element element) {
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
