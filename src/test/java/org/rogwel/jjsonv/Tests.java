package org.rogwel.jjsonv;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.rogwel.jjsonv.node.NodeParser;
import org.rogwel.jjsonv.node.impl.JacksonNodeParser;
import org.rogwel.jjsonv.validators.ValidationContext;
import org.rogwel.jjsonv.validators.trace.ValidationException;

public class Tests {

	private final File basicSchema = new File(System.getProperty("user.dir") + "/src/test/resources/files/basic.jsons");
	private final File basicCustomSchema = new File(System.getProperty("user.dir") + "/src/test/resources/files/basic-custom.jsons");
	private final File basicJson = new File(System.getProperty("user.dir") + "/src/test/resources/files/basic.json");
	private final File basicJsonError = new File(System.getProperty("user.dir") + "/src/test/resources/files/basic-error.json");

	private final NodeParser parser = new JacksonNodeParser();
	private final SchemaFactory factory = new SchemaFactory();

	/**
	 * Adds our own custom made validator
	 */
	@Before
	public void addValidator() throws Exception {
		factory.add("test", TestValidator.class);
	}

	/**
	 * Tests a validation that should succeed.
	 */
	@Test
	public void testValidation() throws Exception {
		Schema schema = factory.create(basicSchema);
		schema.validate(parser.read(basicJson));
	}

	/**
	 * Tests a validation that should fail, and then tests if the failure trace is correct.
	 */
	@Test
	public void testValidationTrace() throws Exception {
		try {
			Schema schema = factory.create(basicSchema);
			schema.validate(parser.read(basicJsonError));
		} catch (ValidationException e) {
			assertEquals("model.members[1].size", e.toString());
		}
	}

	/**
	 * Tests if our custom test Validator was registered, loaded, and if it correctly added data to the resulting ValidationContext
	 */
	@Test
	public void testValidationContext() throws Exception {
		// Load custom schema
		Schema schema = factory.create(basicCustomSchema);
		// Get the result of validation
		ValidationContext context = schema.validate(parser.read(basicJson));
		@SuppressWarnings("unchecked")
		Map<String, String> members = (Map<String, String>) context.get("Members");
		assertEquals("Data1", members.get("Member1"));
		assertEquals("Data2", members.get("Member2"));
	}

	/**
	 * Loads a schema file, writes it using SchemaWriter, then compares written to original.
	 */
	@Test
	public void testSchemaWriter() throws Exception {
		SchemaParser parser = new SchemaParser();
		SchemaWriter writer = new SchemaWriter();
		// Read and write file
		FileReader reader = new FileReader(basicCustomSchema);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writer.write(parser.load(reader), new PrintWriter(baos));
		reader.close();
		// Read each line and compare
		BufferedReader readerA = new BufferedReader(new FileReader(basicCustomSchema));
		BufferedReader readerB = new BufferedReader(new StringReader(new String(baos.toByteArray())));
		String a = readerA.readLine();
		String b = readerB.readLine();
		while (a != null && b != null) {
			Assert.assertEquals(a, b);
			a = readerA.readLine();
			b = readerB.readLine();
		}
		Assert.assertEquals(a, b);
		readerA.close();
		readerB.close();
	}

}
