{ pkgs ? import <nixpkgs> { } }:

let
  unstable = import (fetchTarball https://github.com/NixOS/nixpkgs/archive/nixos-unstable.tar.gz) { };
  utybonp = import (fetchTarball https://github.com/utybo/nixpkgs/archive/refs/heads/gradle-toolchain-via-properties.tar.gz) { };
in

pkgs.mkShell {
  nativeBuildInputs = [
    unstable.nodePackages.pnpm
    (utybonp.gradle.override { javaToolchains = [ pkgs.jdk11 pkgs.jdk17 ]; })
  ];
}
