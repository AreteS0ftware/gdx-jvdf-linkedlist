package it.aretesoftware.gdx.jvdf;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author Brendan Heinonen
 * modified by AreteS0ftware
 */
public class TestGdxVDFParser extends GdxBaseTest {

    private final GdxVDFParser parser = new GdxVDFParser();
    private final String sample = getFileContents("resources/sample.txt");
    private final String sample_multimap = getFileContents("resources/sample_multimap.txt");
    private final String sample_types = getFileContents("resources/sample_types.txt");
    private final String sample_arrays = getFileContents("resources/sample_arrays.txt");
    private final String VDF_SIMPLE_TEST = "key value";
    private final String VDF_SIMPLE_TEST_RESULT = "value";


    @Test
    public void testSimple() {
        Assert.assertEquals(VDF_SIMPLE_TEST_RESULT, parser.parse(VDF_SIMPLE_TEST).getString("key"));
    }

    private static final String VDF_QUOTES_TEST = "\"key with space\" \"value with space\"";
    private static final String VDF_QUOTES_TEST_RESULT = "value with space";

    @Test
    public void testQuotes() {
        Assert.assertEquals(VDF_QUOTES_TEST_RESULT, parser.parse(VDF_QUOTES_TEST).getString("key with space"));
    }

    private static final String VDF_ESCAPE_TEST = "\"key with \\\"\" \"value with \\\" \" \"newline\" \"val\\n\\nue\"";
    private static final String VDF_ESCAPE_TEST_RESULT = "value with \" ";

    @Test
    public void testEscape() {
        GdxVDFNode node = parser.parse(VDF_ESCAPE_TEST);
        Assert.assertEquals(VDF_ESCAPE_TEST_RESULT, node.getString("key with \""));
        Assert.assertEquals("val\n\nue", node.getString("newline"));
    }

    private static final String VDF_NULLKV_TEST = "\"key\" \"\" \"spacer\" \"spacer\" \"\" \"value\"";

    @Test
    public void testNullKeyValue() {
        GdxVDFNode node = parser.parse(VDF_NULLKV_TEST);
        Assert.assertEquals("", node.getString("key"));
        Assert.assertEquals("value", node.getString(""));
    }

    private static final String VDF_SUBSEQUENTKV_TEST = "\"key\"\"value\"";

    @Test
    public void testSubsequentKeyValue() {
        GdxVDFNode node = parser.parse(VDF_SUBSEQUENTKV_TEST);
        Assert.assertEquals("value", node.getString("key"));
    }


    private static final String VDF_UNDERFLOW_TEST = "root_node { child_node { key value }";

    @Test(expected = GdxVDFParseException.class)
    public void testUnderflow() {
        parser.parse(VDF_UNDERFLOW_TEST);
    }

    private static final String VDF_OVERFLOW_TEST = "root_node { child_node { key value } } }";

    @Test(expected = GdxVDFParseException.class)
    public void testOverflow() {
        parser.parse(VDF_OVERFLOW_TEST);
    }

    private static final String VDF_CHILD_TEST = "root { child { key value } }";
    private static final String VDF_CHILD_TEST_RESULT = "value";

    @Test
    public void testChild() {
        Assert.assertEquals(VDF_CHILD_TEST_RESULT, parser.parse(VDF_CHILD_TEST)
                .get("root")
                .get("child")
                .getString("key"));
    }

    @Test
    public void testSample() {
        GdxVDFNode root = parser.parse(sample);

        Assert.assertEquals(GdxVDFNode.class, root.get("root_node").getClass());
        Assert.assertEquals("value1", root
                .get("root_node")
                .get("first_sub_node")
                .getString("first"));
        Assert.assertEquals("value2", root
                .get("root_node")
                .get("first_sub_node")
                .getString("second"));
        Assert.assertEquals("value3", root
                .get("root_node")
                .get("second_sub_node")
                .getString("third"));
        Assert.assertEquals("value4", root
                .get("root_node")
                .get("second_sub_node")
                .get("third_sub_node")
                .getString("fourth"));

        // first_sub_node
        root = root.get("root_node");
        GdxVDFNode subNode = root.get("first_sub_node");
        GdxVDFNode node = subNode.get("first");
        Assert.assertEquals("first", node.name());
        Assert.assertEquals("value1", node.asString());
        node = subNode.get(1);
        Assert.assertEquals("second", node.name());
        Assert.assertEquals("value2", node.asString());
        // second_sub_node
        subNode = root.get("second_sub_node");
        node = subNode.get(1);
        Assert.assertEquals("third", node.name());
        Assert.assertEquals("value3", node.asString());
        // third_sub_node
        node = subNode.getChild("third_sub_node");
        Assert.assertEquals("fourth", node.name());
        Assert.assertEquals("value4", node.asString());
    }

    @Test
    public void testMultimap() {
        GdxVDFNode root = parser.parse(sample_multimap);

        Assert.assertEquals(2, root.get("root_node").sizeOf("sub_node"));
        Assert.assertEquals("value1", root
                .get("root_node")
                .get("sub_node", 0)
                .getString("key"));
        Assert.assertEquals("value4", root
                .get("root_node")
                .get("sub_node", 1)
                .get("key", 1).asString());

        // sub_node
        List<GdxVDFNode> nodes = root.get("root_node").asArray("sub_node");
        Assert.assertEquals(2, nodes.size());
        // first
        GdxVDFNode subNode = nodes.get(0);
        GdxVDFNode node = subNode.get("key");
        Assert.assertEquals("key", node.name());
        Assert.assertEquals("value1", node.asString());
        node = node.next();
        Assert.assertEquals("key", node.name());
        Assert.assertEquals("value2", node.asString());
        // second
        subNode = nodes.get(1);
        node = subNode.get(0);
        Assert.assertEquals("key", node.name());
        Assert.assertEquals("value3", node.asString());
        node = subNode.get("key", 1);
        Assert.assertEquals("value4", node.asString());
    }

    //

    @Test
    public void test() {
        GdxVDFNode node = parser.parse(sample_types).get("root_node");
        // get by name
        Assert.assertEquals(123456, node.getLong("long"));
        Assert.assertEquals(123456, node.getInt("long"));
        Assert.assertEquals(100, node.getInt("int"));
        Assert.assertEquals(100, node.getLong("int"));
        Assert.assertEquals(100, node.getShort("int"));
        Assert.assertEquals(100, node.getByte("int"));
        Assert.assertEquals(10E2, node.getDouble("double"), 0f);
        Assert.assertEquals(1000, node.getFloat("double"), 0f);
        Assert.assertEquals(123.456d, node.getDouble("float"), 0f);
        Assert.assertEquals(123.456f, node.getFloat("float"), 0f);
        Assert.assertTrue(node.getBoolean("boolean"));
        Assert.assertEquals("true", node.getString("boolean"));
        Assert.assertEquals("Test!", node.getString("string"));
        Assert.assertEquals('a', node.getChar("char"));
        Assert.assertEquals("a", node.getString("char"));
        Assert.assertEquals(Color.WHITE, node.getColor("color"));
        Assert.assertEquals(new Vector3(1, 1, 1), node.getVector3("vec3"));
        Assert.assertEquals(new Vector2(0, 1), node.getVector2("vec2"));
        Assert.assertEquals(GdxEnumTest.first, node.getEnum("enum", GdxEnumTest.class));
        // get by index
        Assert.assertEquals("Test!", node.getString(5));
        Assert.assertEquals(123.456f, node.getFloat(3), 0);
        Assert.assertEquals(10e2, node.getDouble(2), 0);
        Assert.assertEquals(123456L, node.getLong(0), 0);
        Assert.assertEquals(100, node.getInt(1), 0);
        Assert.assertTrue(node.getBoolean(4));
        Assert.assertEquals(100, node.getByte(1));
        Assert.assertEquals(100, node.getShort(1));
        Assert.assertEquals('a', node.getChar(6));
        Assert.assertEquals(Color.WHITE, node.getColor(7));
        Assert.assertEquals(new Vector3(1, 1, 1), node.getVector3(8));
        Assert.assertEquals(new Vector2(0, 1), node.getVector2(9));
    }

    @Test
    public void testArrays() {
        GdxVDFNode root = parser.parse(sample_arrays).get("root_node");
        // VDFNode
        List<GdxVDFNode> vdfValues = root.asArray("vdfValues");
        Assert.assertEquals(4, vdfValues.size());
        Assert.assertEquals(0.1f, vdfValues.get(0).asFloat(), 0f);
        Assert.assertTrue(vdfValues.get(1).asBoolean());
        Assert.assertEquals("Test!", vdfValues.get(2).asString());
        Assert.assertEquals(1, vdfValues.get(3).asLong());
        // String
        List<String> stringValues = root.asStringArray("vdfValues");
        Assert.assertEquals(4, stringValues.size());
        Assert.assertEquals("0.1", stringValues.get(0));
        Assert.assertEquals("true", stringValues.get(1));
        Assert.assertEquals("Test!", stringValues.get(2));
        Assert.assertEquals("1", stringValues.get(3));
        // Double
        List<Double> doubleValues = root.asDoubleArray("doubleValues");
        Assert.assertEquals(3, doubleValues.size());
        Assert.assertEquals(1000d, doubleValues.get(0), 0);
        Assert.assertEquals(0.1d, doubleValues.get(1), 0);
        Assert.assertEquals(-10d, doubleValues.get(2), 0);
        // Float
        List<Float> floatValues = root.asFloatArray("doubleValues");
        Assert.assertEquals(3, floatValues.size());
        Assert.assertEquals(1000f, floatValues.get(0), 0);
        Assert.assertEquals(0.1f, floatValues.get(1), 0.001f);
        Assert.assertEquals(-10f, floatValues.get(2), 0);
        // Long
        List<Long> longValues = root.asLongArray("longValues");
        Assert.assertEquals(3, longValues.size());
        Assert.assertEquals(1L, longValues.get(0), 0);
        Assert.assertEquals(+10L, longValues.get(1), 0);
        Assert.assertEquals(-100L, longValues.get(2), 0);
        // Int
        List<Integer> intValues = root.asIntArray("longValues");
        Assert.assertEquals(3, intValues.size());
        Assert.assertEquals(1L, intValues.get(0), 0);
        Assert.assertEquals(+10L, intValues.get(1), 0);
        Assert.assertEquals(-100L, intValues.get(2), 0);
        // Short
        List<Short> shortValues = root.asShortArray("longValues");
        Assert.assertEquals(3, shortValues.size());
        Assert.assertEquals(1, shortValues.get(0), 0);
        Assert.assertEquals(+10, shortValues.get(1), 0);
        Assert.assertEquals(-100, shortValues.get(2), 0);
        // Byte
        List<Byte> byteValues = root.asByteArray("longValues");
        Assert.assertEquals(3, byteValues.size());
        Assert.assertEquals(1, byteValues.get(0), 0);
        Assert.assertEquals(+10, byteValues.get(1), 0);
        Assert.assertEquals(-100, byteValues.get(2), 0);
        // Char
        List<Character> charValues = root.asCharArray("charValues");
        Assert.assertEquals(3, charValues.size());
        Assert.assertEquals('a', charValues.get(0), 0);
        Assert.assertEquals('b', charValues.get(1), 0);
        Assert.assertEquals('c', charValues.get(2), 0);
        // Long -> Char
        charValues = root.asCharArray("longValues");
        Assert.assertEquals(3, charValues.size());
        Assert.assertEquals(1, charValues.get(0), 0);
        Assert.assertEquals(10, charValues.get(1), 0);
        Assert.assertEquals(65436, charValues.get(2), 0);
        // Boolean
        List<Boolean> booleanValues = root.asBooleanArray("booleanValues");
        Assert.assertTrue(booleanValues.get(0));
        Assert.assertFalse(booleanValues.get(1));
        // Boolean -> String
        List<String> booleanToStringValues = root.asStringArray("booleanValues");
        Assert.assertEquals("true", booleanToStringValues.get(0));
        Assert.assertEquals("false", booleanToStringValues.get(1));
        // Color
        List<Color> colorValues = root.asColorArray("colorValues");
        Assert.assertEquals(Color.WHITE, colorValues.get(0));
        Assert.assertEquals(Color.CLEAR, colorValues.get(1));
        Assert.assertEquals(Color.BLUE, colorValues.get(2));
        // Vector3
        List<Vector3> vec3Values = root.asVector3Array("vec3Values");
        Assert.assertEquals(new Vector3(), vec3Values.get(0));
        Assert.assertEquals(new Vector3(1, 0, 0), vec3Values.get(1));
        Assert.assertEquals(new Vector3(1, 1, 1), vec3Values.get(2));
        // Vector2
        List<Vector2> vec2Values = root.asVector2Array("vec2Values");
        Assert.assertEquals(new Vector2(), vec2Values.get(0));
        Assert.assertEquals(new Vector2(0, 1), vec2Values.get(1));
        Assert.assertEquals(new Vector2(1, 0), vec2Values.get(2));
        // Enum
        List<GdxEnumTest> enumValues = root.asEnumArray("enumValues", GdxEnumTest.class);
        Assert.assertEquals(GdxEnumTest.fifth, enumValues.get(0));
        Assert.assertEquals(GdxEnumTest.fourth, enumValues.get(1));
        Assert.assertEquals(GdxEnumTest.third, enumValues.get(2));
    }

    @Test
    public void testDefaultValue() {
        GdxVDFNode node = new GdxVDFNode();
        // get by name
        Assert.assertEquals("defaultValue", node.getString("", "defaultValue"));
        Assert.assertEquals(10f, node.getFloat("", 10f), 0);
        Assert.assertEquals(10d, node.getDouble("", 10d), 0);
        Assert.assertEquals(10, node.getLong("", 10), 0);
        Assert.assertEquals(10, node.getInt("", 10), 0);
        Assert.assertTrue(node.getBoolean("", true));
        Assert.assertEquals(10, node.getByte("", (byte) 10), 0);
        Assert.assertEquals(10, node.getShort("", (short) 10), 0);
        Assert.assertEquals('a', node.getChar("", 'a'));
        Assert.assertEquals(Color.WHITE, node.getColor("", Color.WHITE));
        Assert.assertEquals(new Vector3(1, 1, 1), node.getVector3("", new Vector3(1, 1, 1)));
        Assert.assertEquals(new Vector2(1, 1), node.getVector2("", new Vector2(1, 1)));
        Assert.assertEquals(GdxEnumTest.second, node.getEnum("", GdxEnumTest.second));
        // get by name & index
        Assert.assertEquals("defaultValue", node.getStringOfIndex("", 1, "defaultValue"));
        Assert.assertEquals(10f, node.getFloatOfIndex("", 1, 10f), 0);
        Assert.assertEquals(10d, node.getDoubleOfIndex("",  1, 10d), 0);
        Assert.assertEquals(10L, node.getLongOfIndex("", 1, 10L), 0);
        Assert.assertEquals(10, node.getIntOfIndex("", 1, 10), 0);
        Assert.assertTrue(node.getBooleanOfIndex("", 1, true));
        Assert.assertEquals(10, node.getByteOfIndex("", 1, (byte) 10), 0);
        Assert.assertEquals(10, node.getShortOfIndex("", 1, (short) 10), 0);
        Assert.assertEquals('a', node.getCharOfIndex("", 1, 'a'));
        Assert.assertEquals(Color.WHITE, node.getColorOfIndex("", 1, Color.WHITE));
        Assert.assertEquals(new Vector3(1, 1, 1), node.getVector3OfIndex("", 1, new Vector3(1, 1, 1)));
        Assert.assertEquals(new Vector2(1, 1), node.getVector2OfIndex("", 1, new Vector2(1, 1)));
        Assert.assertEquals(GdxEnumTest.second, node.getEnumOfIndex("", 1, GdxEnumTest.second));
        // as
        Assert.assertEquals("defaultValue", node.asString("defaultValue"));
        Assert.assertEquals(10f, node.asFloat(10f), 0);
        Assert.assertEquals(10d, node.asDouble(10d), 0);
        Assert.assertEquals(10L, node.asLong(10L), 0);
        Assert.assertTrue(node.asBoolean(true));
        Assert.assertEquals(10, node.asByte((byte) 10), 0);
        Assert.assertEquals(10, node.asShort((short) 10), 0);
        Assert.assertEquals(97, node.asShort('a'));
        Assert.assertEquals('a', node.asChar('a'));
        Assert.assertEquals(Color.WHITE, node.asColor(Color.WHITE));
        Assert.assertEquals(new Vector3(1, 1, 1), node.asVector3(new Vector3(1, 1, 1)));
        Assert.assertEquals(new Vector2(1, 1), node.asVector2(new Vector2(1, 1)));
        Assert.assertEquals(GdxEnumTest.fifth, node.asEnum(GdxEnumTest.fifth));

    }

}
