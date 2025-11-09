build:
  ./gradlew lwjgl3:jar

run:
  ./gradlew lwjgl3:run

clean:
  ./gradlew clean

docs:
  doxygen

docs-serve: docs
  pushd docs/html && python3 -m http.server && popd

docs-pdf: docs
  pushd docs/latex && make && popd