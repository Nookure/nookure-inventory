package com.nookure.core.inv.test;

import com.nookure.core.inv.parser.GuiLayout;
import com.nookure.core.inv.parser.item.Item;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

public class XMLParsingTest {
  @Test
  public void xmlTest() throws JAXBException, ParserConfigurationException, SAXException {
    try (InputStream xmlStream = XMLParsingTest.class.getClassLoader().getResourceAsStream("GuiLayoutTest.xml")) {
      GuiLayout guiLayout = ActionTest.guiLayout(xmlStream);

      Assertions.assertEquals("<green>The</green><red>Inventory</red><blue>Title</blue>", guiLayout.head().title().title());
      Assertions.assertTrue(guiLayout.head().title().tl());
      Assertions.assertEquals(3, guiLayout.head().rows());
      Assertions.assertEquals("nookure.inventory.test", guiLayout.head().permission());

      Item first = guiLayout.items().itemList().get(0);
      Assertions.assertEquals("item-id", first.id());
      Assertions.assertEquals(2, first.slot());
      Assertions.assertEquals("STONE", first.material());
      Assertions.assertEquals("<red>Stone</red>", first.name());
      Assertions.assertEquals(
          List.of("<red>Stone</red>", "<blue>Line 2</blue>", "<green>Line 3</green>"),
          first.literalLore().loreLines()
      );

      Item second = guiLayout.items().itemList().get(1);
      Assertions.assertEquals(List.of("<red>Magic Stone </red>", "<blue>Line 2</blue>"), second.lore().loreLines());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
