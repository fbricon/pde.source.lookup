# PDE Source Lookup

[![Build Status](https://travis-ci.org/fbricon/pde.source.lookup.svg?branch=master)](https://travis-ci.org/fbricon/pde.source.lookup)

Proof of Concept to automatically fetch missing source bundles from enabled p2 repositories, when opening a bundle jar (similar to m2e).
It's also possible to fetch all sources from the `Plug-in Dependencies` classpath container, with a right-click and selecting `Download Bundle Sources`.

Sources are cached to `~/.eclipse/org.jboss.tools.pde.sourcelookup.core/sources`. That folder can be changed in `Preferences` > `Plug-in Development` > `Source Lookup`.

Because the `Plug-in Dependencies` classpath container [doesn't keep new attached sources on workspace restart](https://bugs.eclipse.org/bugs/show_bug.cgi?id=492204),
the PDE Source Lookup plugin automatically reattaches thoses sources upon restart.
This behavior can be disabled in `Preferences` > `Plug-in Development` > `Source Lookup`.

## installation
- Requires an Eclipse Mars.2 or more recent installation (might work on older versions)
- Requires Java 8 to run.
- In Eclipse, either:
  - go to `Help` > `Eclipse Marketplace…` to search PDE Source Lookup from the [Eclipse Marketplace](https://marketplace.eclipse.org/content/pde-source-lookup)
  - go to `Help` > `Install New Software...` and add one of these p2 repositories:
      - Stable: https://dl.bintray.com/fbricon/poc/pde.source.lookup/0.0.4.201612181944
      - CI: http://download.jboss.org/jbosstools/builds/staging/pde.source.lookup/all/repo/


Licensed under the [EPL-2.0](https://www.eclipse.org/legal/epl-2.0/).
