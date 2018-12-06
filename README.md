![EnumToYaml](https://dl.dropbox.com/s/qy5drl67j1ofnhe/EnumToYaml.png?dl=0)
A library, which allows you to turn a JAVA enum to a configuration.

## What is EnumToYaml?
EnumToYaml is a program created by Articdive (Lukas Mansour).  
It's an open-source library that allows you to create configurations with JAVA's enums.  
The aim of EnumToYaml is to make YAML configurations easy to use, easily accessible and useable with minimal code.  

## How can I use EnumToYaml?
EnumToYaml is primarly available to maven users.  
To add it to your maven project use the following repository and dependency:  
    <repository>
      <id>articdive-repo</id>
      <url>https://nexus.articdive.de/repository/maven-public/</url>
    </repository>
  
    <dependency>
      <groupId>de.articdive</groupId>
      <artifactId>EnumToYaml</artifactId>
      <version>1.0-SNAPSHOT</version>
      <scope>compile</scope>
    </dependency>
### Usage in Java:
EnumToYaml will require a file and an enum to work at all.  

The enum must implement ConfigurationEnum and might look something like this:  
```java
import de.articdive.enum_to_yaml.interfaces.ConfigurationEnum;

public enum MyCreativeEnum implements ConfigurationEnum {
    MY_EXAMPLE_NODE("node", "My Value", "# Hey this is a node."),
    MY_EXAMPLE_PARENT_NODE("parent", "", "# Hey this is a parent node."),
    MY_EXAMPLE_CHILD_NODE("parent.node", "My Child Value", "# Hey this is a child node");

    private String path;
    private Object defaultValue;
    private String[] comments;

    MyCreativeEnum(String path, Object defaultValue, String... comments) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.comments = comments;
    }


    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String[] getComments() {
        return comments;
    }
}

```
Then create an EnumConfiguration with the EnumConfigurationBuilder, which requires an output File and the enum.  
Then set any options needed and finish it off with a call to the build Method.  
```java
EnumConfiguration enumConfiguration = new EnumConfigurationBuilder(File file, MyCreativeEnum.class).build();
```
To get any values use the get method:  
Please note there are methods like getString, getInteger e.t.c, which can be used to get different data types.  
```java
Object value = enumConfiguration.get(MyCreativeEnum.MY_EXAMPLE_NODE);
```
To set any values use the set method:  
```java
enumConfiguration.set(MyCreativeEnum.MY_EXAMPLE_NODE, newValue);
```

## I have a question, issue, request, suggestion or similar.
Please [open an issue here on GitHub](https://github.com/Articdive/EnumToYaml/issues/new).

## I want to donate.
I highly appreciaite donations, especially when others make money by using my library.  
I will not force you to donate nor will I treat you otherwise for not.  
Please only donate if you are over the age of 18 or have permission from a legal guardian and use your own money.  

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=2GDHSJK2FDDF6)
