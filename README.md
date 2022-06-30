# PDE Source Lookup
[![Build](https://github.com/fbricon/pde.source.lookup/actions/workflows/ci.yaml/badge.svg)](https://github.com/fbricon/pde.source.lookup/actions/workflows/ci.yaml)

Proof of Concept to automatically fetch missing source bundles from enabled p2 repositories, when opening a bundle jar (similar to m2e).
It's also possible to fetch all sources from the `Plug-in Dependencies` classpath container, with a right-click and selecting `Download Bundle Sources`.

Sources are cached to `~/.eclipse/org.jboss.tools.pde.sourcelookup.core/sources`. That folder can be changed in `Preferences` > `Plug-in Development` > `Source Lookup`.

Because the `Plug-in Dependencies` classpath container [doesn't keep new attached sources on workspace restart](https://bugs.eclipse.org/bugs/show_bug.cgi?id=492204),
the PDE Source Lookup plugin automatically reattaches thoses sources upon restart.
This behavior can be disabled in `Preferences` > `Plug-in Development` > `Source Lookup`.

## Installation
- Requires an Eclipse Mars.2 or more recent installation (might work on older versions)
- Requires Java 8 to run.

_PDE Source Lookup_ is available in the [Eclipse Marketplace](https://marketplace.eclipse.org/content/pde-source-lookup). Drag the following button to your running Eclipse workspace. (⚠️ *Requires the Eclipse Marketplace Client*)

[![Drag to your running Eclipse* workspace. *Requires Eclipse Marketplace Client](https://marketplace.eclipse.org/sites/all/themes/solstice/public/images/marketplace/btn-install.svg)](http://marketplace.eclipse.org/marketplace-client-intro?mpc_install=5391163 "Drag to your running Eclipse* workspace. *Requires Eclipse Marketplace Client")

Alternatively, in Eclipse:

- open Help > Install New Software...
- work with: `https://github.com/fbricon/pde.source.lookup/releases/download/latest/`
- expand the category and select the `PDE Source Lookup` Eclipse Feature
- proceed with the installation
- restart Eclipse

## License
Licensed under the [EPL-2.0](https://www.eclipse.org/legal/epl-2.0/).
