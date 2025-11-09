{
  description = "A flake";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs?ref=nixos-unstable";
    nixpkgsStable.url = "github:nixos/nixpkgs?ref=nixos-23.05";
  };

  outputs =
    { nixpkgs, nixpkgsStable, ... }:
    let
      system = "x86_64-linux";
    in
    {
      devShells."${system}".default =
        let
          pkgs = import nixpkgs {
            inherit system;
            config.allowUnfree = true;
          };
          pkgsStable = import nixpkgsStable {
            inherit system;
            config.allowUnfree = true;
          };
        in
        pkgs.mkShell {
          packages = (with pkgs; [
            python312
            gradle
            jdk21
            doxygen
            graphviz
            just
            steam-run
          ]) ++ (with pkgsStable; [
            texlive.combined.scheme-full
          ]);
          env = {
            JAVA_HOME = "${pkgs.jdk21}/lib/openjdk";
            GRADLE_OPTS = "-Dorg.gradle.java.home=${pkgs.jdk21}/lib/openjdk";
          };
          buildInputs = [ pkgs.bashInteractive ];
          shellHook = '''';
        };
    };
}
