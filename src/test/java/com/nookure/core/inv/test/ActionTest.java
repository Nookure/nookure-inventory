package com.nookure.core.inv.test;

import com.nookure.core.inv.NookureInventoryEngine;
import com.nookure.core.inv.parser.GuiLayout;
import com.nookure.core.inv.parser.item.action.Action;
import com.nookure.core.inv.parser.item.Item;
import io.pebbletemplates.pebble.loader.StringLoader;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ActionTest {
  @Test
  public void action() throws JAXBException, ParserConfigurationException, SAXException {
    try (InputStream xmlStream = ActionTest.class.getClassLoader().getResourceAsStream("GuiLayoutTest.xml")) {
      GuiLayout guiLayout = guiLayout(xmlStream);

      Assertions.assertNotNull(guiLayout);
      Assertions.assertEquals("<green>The</green><red>Inventory</red><blue>Title</blue>", guiLayout.head().title().title());
      Assertions.assertTrue(guiLayout.head().title().tl());
      Assertions.assertEquals(3, guiLayout.head().rows());
      Assertions.assertEquals(2, guiLayout.items().itemList().size());

      Item secondItem = guiLayout.items().itemList().get(1);
      Assertions.assertNotNull(secondItem.actions());
      Assertions.assertEquals(2, secondItem.actions().actionsList().size());
      Assertions.assertEquals("CLOSE_INVENTORY", secondItem.actions().actionsList().get(0).type());
      Assertions.assertEquals("RUN_COMMAND_AS_PLAYER", secondItem.actions().actionsList().get(1).type());
      Assertions.assertEquals("say Hello", secondItem.actions().actionsList().get(1).value());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static GuiLayout guiLayout(InputStream xmlStream) throws JAXBException, ParserConfigurationException, SAXException {
    NookureInventoryEngine engine = new NookureInventoryEngine(new StringLoader());
    StringBuilder textBuilder = new StringBuilder();
    Assertions.assertNotNull(xmlStream);

    try (Reader reader = new BufferedReader(new InputStreamReader(xmlStream, StandardCharsets.UTF_8))) {
      int c;
      while ((c = reader.read()) != -1) {
        textBuilder.append((char) c);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return engine.parseLayout(textBuilder.toString());
  }
}
