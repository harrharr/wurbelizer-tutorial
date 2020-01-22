/*
 * Wurbelizer tutorial.
 */

package com.example.wurblets;

import org.wurbelizer.wurbel.WurbelException;
import org.wurbelizer.wurbel.WurbelHelper;
import org.wurbelizer.wurblet.AbstractJavaWurblet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Base class for the {@code @DTO}-wurblet.
 * <p>
 * Parses wurblet args and the model.
 */
public class DTOWurblet extends AbstractJavaWurblet {

  /**
   * DTO property.
   */
  protected class Property {

    private final String type;                // the java type
    private final String name;                // the property name
    private final String comment;             // the comment

    public Property(String type, String name, String comment) {
      this.type = type;
      this.name = name;
      this.comment = comment == null || comment.isEmpty() ? "the " + name + " property" : comment.trim();
    }

    public String getType() {
      return type;
    }

    public String getName() {
      return name;
    }

    public String getComment() {
      return comment;
    }

    public String getGetterName() {
      return ("boolean".equals(type) ? "is" : "get") + firstToUpper(name);
    }

    public String getSetterName() {
      return "set" + firstToUpper(name);
    }

  }

  protected String filename;            // filename of the here-doc holding the model (usually given as "$> .$filename")
  protected List<Property> properties;  // the DTO properties

  @Override
  public void run() throws WurbelException {
    super.run();

    if (getContainer().getArgs().length == 0) {
      throw new WurbelException("usage: @wurblet <guardname> DTO <filename>");
    }
    filename = getContainer().getArgs()[0];

    properties = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(WurbelHelper.openReader(filename))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (!line.isEmpty() && !line.startsWith("#")) {
          StringTokenizer stok = new StringTokenizer(line);
          String type = null;
          String name = null;
          StringBuilder comment = new StringBuilder();
          while (stok.hasMoreTokens()) {
            String token = stok.nextToken();
            if (type == null) {
              type = token;
            }
            else if (name == null) {
              name = token;
            }
            else {
              if (comment.length() > 0) {
                comment.append(' ');
              }
              comment.append(token);
            }
          }
          if (type == null || name == null) {
            throw new WurbelException("property line too short: '" + line + "'");
          }
          properties.add(new Property(type, name, comment.toString()));
        }
      }

      // validate
      for (Property property : properties) {
        if (property.getType().isEmpty()) {
          throw new WurbelException("missing property type");
        }
        if (property.getName().isEmpty()) {
          throw new WurbelException("missing property name");
        }
      }
    }
    catch (IOException ex) {
      throw new WurbelException("reading model " + filename + " failed", ex);
    }
  }

  private String firstToUpper(String str) {
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }

}
