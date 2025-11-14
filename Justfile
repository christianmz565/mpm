build:
  ./gradlew lwjgl3:jar

[unix]
run count="1":
  ./scripts/run-many.sh {{count}}

[windows]
run count="1":
  ./scripts/run-many.ps1 {{count}}

clean:
  ./gradlew clean

docs:
  doxygen

docs-serve: docs
  pushd docs/html && python3 -m http.server && popd

docs-pdf: docs
  pushd docs/latex && make && popd