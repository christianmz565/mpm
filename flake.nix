{
  description = "A flake";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs?ref=nixos-unstable";
  };

  outputs =
    { nixpkgs, ... }:
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
        in
        pkgs.mkShell {
          packages = with pkgs; [
            gradle
            jdk21
          ];
          env = {
            JAVA_HOME = "${pkgs.jdk21}/lib/openjdk";
            GRADLE_OPTS = "-Dorg.gradle.java.home=${pkgs.jdk21}/lib/openjdk";
          };
          buildInputs = [ pkgs.bashInteractive ];
          shellHook = '''';
        };
    };
}
