# PDE Source Lookup

Proof of Concept to automatically fetch missing source bundles from enabled p2 repositories, when opening a bundle jar (similar to m2e).

Sources are cached to `~/.eclipse/org.jboss.tools.pde.sourcelookup.core/sources`

## installation
- Requires an Eclipse Mars.2 or Neon installation (might work on older versions)
- Requires Java 8 to run. 
- In Eclipse, go to `Help` > `Install New Software...` and add this p2 repository:
  http://download.jboss.org/jbosstools/builds/staging/pde.source.lookup/all/repo/


Licensed under the EPL
