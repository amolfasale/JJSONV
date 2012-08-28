package skillable.jjsonv.validators.elements;

import java.util.regex.Pattern;

import org.codehaus.jackson.JsonNode;

public class RegexValidator extends ElementValidator {

	public boolean ok(JsonNode node) {
		return node.isTextual() && pattern.matcher(node.asText()).find();
	}

	private final Pattern pattern;

	public RegexValidator(String regex) {
		pattern = Pattern.compile(regex);
	}

}