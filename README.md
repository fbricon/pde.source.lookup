# PDE Source Lookup

[![Build Status](https://travis-ci.org/fbricon/pde.source.lookup.svg?branch=master)](https://travis-ci.org/fbricon/pde.source.lookup)

Proof of Concept to automatically fetch missing source bundles from enabled p2 repositories, when opening a bundle jar (similar to m2e).

Sources are cached to `~/.eclipse/org.jboss.tools.pde.sourcelookup.core/sources`

## installation
- Requires an Eclipse Mars.2 or Neon installation (might work on older versions)
- Requires Java 8 to run. 
- In Eclipse, go to `Help` > `Install New Software...` and add one of these p2 repositories:
  - Stable: https://dl.bintray.com/fbricon/poc/pde.source.lookup/0.0.2.201608010932/
  - CI: http://download.jboss.org/jbosstools/builds/staging/pde.source.lookup/all/repo/


Licensed under the EPL
