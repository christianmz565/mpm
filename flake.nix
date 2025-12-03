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
          runtimeLibs = with pkgs; lib.makeLibraryPath [
            glfw
            libGL
            xorg.libX11
            xorg.libXcursor
            xorg.libXext
            xorg.libXrandr
            xorg.libXxf86vm
          ];
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
            texlive.combined.scheme-full
          ]);
          buildInputs = with pkgs; [
            bashInteractive
          ];
          shellHook = ''
            export LD_LIBRARY_PATH=${runtimeLibs}:$LD_LIBRARY_PATH
          '';
        };
    };
}
