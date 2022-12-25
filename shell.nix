{ pkgs ? import <nixpkgs> { } }:

let
  unstable = import (fetchTarball https://github.com/NixOS/nixpkgs/archive/nixos-unstable.tar.gz) { };
  utybonp = import (fetchTarball https://github.com/utybo/nixpkgs/archive/refs/heads/gradle-toolchain-via-properties.tar.gz) { };
  toolchains = [ unstable.jdk11 unstable.jdk17 ];
in

pkgs.mkShell {
  nativeBuildInputs = [
    unstable.nodePackages.pnpm
    (utybonp.gradle.override { javaToolchains = [ unstable.jdk11 unstable.jdk17 ]; })
  ];
}
