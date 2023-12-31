package it.aretesoftware.gdx.jvdf;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestVDFGdxWriter extends GdxBaseTest {

    private final GdxVDFWriter writer = new GdxVDFWriter();
    private final GdxVDFPreprocessor preprocessor = new GdxVDFPreprocessor();
    private final String sample = getFileContents("resources/sample.txt");
    private final String sample_types = getFileContents("resources/sample_types.txt");
    private final String sample_arrays = getFileContents("resources/sample_arrays.txt");

    @Test
    public void testSample() {
        writer.writeNodeStart("root_node");
            writer.writeNodeStart("first_sub_node");
                writer.writeValue("first", "value1");
                writer.writeValue("second", "value2");
            writer.writeNodeEnd();
            writer.writeNodeStart("second_sub_node");
                writer.writeNodeStart("third_sub_node");
                    writer.writeValue("fourth", "value4");
                writer.writeNodeEnd();
                writer.writeValue("third", "value3");
            writer.writeNodeEnd();
        writer.writeNodeEnd();
        String first = preprocessor.process(writer.toVDF());
        String second = preprocessor.process(sample);
        Assert.assertEquals(first, second);
    }

    @Test
    public void testWriteValue() {
        writer.writeNodeStart("root_node");
            writer.writeValue("long", (long)123456);
            writer.writeValue("int", 100);
            writer.writeValue("double", 10E2);
            writer.writeValue("float", 123.456f);
            writer.writeValue("boolean", true);
            writer.writeValue("string", "Test!");
            writer.writeValue("char", 'a');
            writer.writeValue("color", new Color(1, 1, 1, 1));
            writer.writeValue("vec3", new Vector3(1, 1, 1));
            writer.writeValue("vec2", new Vector2(0, 1));
            writer.writeValue("enum", GdxEnumTest.first);
        writer.writeNodeEnd();
        GdxVDFParser parser = new GdxVDFParser();
        GdxVDFNode firstNode = parser.parse(writer.toVDF());
        GdxVDFNode secondNode = parser.parse(sample_types);
        Assert.assertEquals(firstNode.toVDF(), secondNode.toVDF());
    }

    @Test
    public void testWriteMultimapValue() {
        writer.writeNodeStart("root_node");
            writer.writeMultimapValue("vdfValues", "0.1", "true", "Test!", "1");
            writer.writeMultimapValue("doubleValues", 10E2d, 0.1d, -10);
            writer.writeMultimapValue("longValues", 1L, +10L, -100L);
            writer.writeMultimapValue("charValues", 'a', 'b', 'c');
            writer.writeMultimapValue("booleanValues", true, false);
            writer.writeMultimapValue("colorValues", new Color(1, 1, 1, 1), new Color(0, 0, 0, 0), new Color(0, 0, 1, 1));
            writer.writeMultimapValue("vec3Values", new Vector3(0, 0, 0), new Vector3(1, 0, 0), new Vector3(1, 1, 1));
            writer.writeMultimapValue("vec2Values", new Vector2(0, 0), new Vector2(0, 1), new Vector2(1, 0));
            writer.writeMultimapValue("enumValues", GdxEnumTest.fifth, GdxEnumTest.fourth, GdxEnumTest.third);
        writer.writeNodeEnd();
        GdxVDFParser parser = new GdxVDFParser();
        GdxVDFNode firstNode = parser.parse(writer.toVDF());
        GdxVDFNode secondNode = parser.parse(sample_arrays);
        Assert.assertEquals(firstNode.toVDF(), secondNode.toVDF());
    }

    @Test
    public void testMisc() {
        writer.writeMultimapValue("floatValues", 1f, 10f, -100f);
        writer.writeMultimapValue("intValues", 1, 10, -100);
        writer.writeMultimapValue("byteValues", (byte)1, (byte)10, (byte)-100);
        writer.writeMultimapValue("shortValues", (short)1, (short)10, (short)-100);
        writer.writeMultimapValue("enumValues", GdxEnumTest.class, "first", "second", "third");
        GdxVDFParser parser = new GdxVDFParser();
        GdxVDFNode node = parser.parse(writer.toVDF());
        // Float
        List<Float> floatValues = node.asFloatArray("floatValues");
        Assert.assertEquals(1f, floatValues.get(0), 0f);
        Assert.assertEquals(10f, floatValues.get(1), 0f);
        Assert.assertEquals(-100f, floatValues.get(2), 0f);
        // Int
        List<Integer> intValues = node.asIntArray("intValues");
        Assert.assertEquals(1, intValues.get(0), 0f);
        Assert.assertEquals(10, intValues.get(1), 0f);
        Assert.assertEquals(-100, intValues.get(2), 0f);
        // Byte
        List<Byte> byteValues = node.asByteArray("byteValues");
        Assert.assertEquals(1, byteValues.get(0), 0);
        Assert.assertEquals(10, byteValues.get(1), 0f);
        Assert.assertEquals(-100, byteValues.get(2), 0f);
        // Short
        List<Short> shortValues = node.asShortArray("shortValues");
        Assert.assertEquals(1, shortValues.get(0), 0);
        Assert.assertEquals(10, shortValues.get(1), 0f);
        Assert.assertEquals(-100, shortValues.get(2), 0f);
        // Enum
        List<GdxEnumTest> enumValues = node.asEnumArray("enumValues", GdxEnumTest.class);
        Assert.assertEquals(GdxEnumTest.first, enumValues.get(0));
        Assert.assertEquals(GdxEnumTest.second, enumValues.get(1));
        Assert.assertEquals(GdxEnumTest.third, enumValues.get(2));
    }

}
