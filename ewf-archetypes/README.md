# sf ewf archetypes

How to create project using sf-ewf archetype (interactive mode)
---

```
mvn archetype:generate -DarchetypeGroupId=io.sunflower.archetypes -DarchetypeArtifactId=ewf-simple -DarchetypeVersion=[REPLACE ME WITH A VALID EWF VERSION]
```

(when asked for ``$name`` during project creation via maven, make sure to use a camel case word such as ``HelloWorld`` as it is used to generate Configuration and Application classess such as ``HelloWorldConfiguration.java`` and ``HelloWorldApplication.java``. Furthermore, do not include any blank space for the same reason!)