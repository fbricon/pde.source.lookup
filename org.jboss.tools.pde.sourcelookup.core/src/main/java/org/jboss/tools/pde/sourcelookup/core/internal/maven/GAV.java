/*******************************************************************************
 * Copyright (c) 2018-2020 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/

package org.jboss.tools.pde.sourcelookup.core.internal.maven;

public class GAV {

  public static GAV UNKNOWN_ARTIFACT = new GAV("_", "_", "_");

  private String groupId;
  private String artifactId;
  private String version;

  public GAV(String groupId, String artifactId, String version) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
  }

  @Override
  public String toString() {
    return getGroupId() + ":" + getArtifactId() + ":" + getVersion();
  }

  public static GAV fromString(String gav) {
    if (gav != null) {
      if ("_:_:_".equals(gav)) {
        return UNKNOWN_ARTIFACT;
      }
      String[] coords = gav.split(":");
      if (coords.length > 2) {
        return new GAV(coords[0], coords[1], coords[2]);
      }
    }
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getArtifactId() == null) ? 0 : getArtifactId().hashCode());
    result = prime * result + ((getGroupId() == null) ? 0 : getGroupId().hashCode());
    result = prime * result + ((getVersion() == null) ? 0 : getVersion().hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    GAV other = (GAV) obj;
    if (getArtifactId() == null) {
      if (other.getArtifactId() != null) {
        return false;
      }
    } else if (!getArtifactId().equals(other.getArtifactId())) {
      return false;
    }
    if (getGroupId() == null) {
      if (other.getGroupId() != null) {
        return false;
      }
    } else if (!getGroupId().equals(other.getGroupId())) {
      return false;
    }
    if (getVersion() == null) {
      if (other.getVersion() != null) {
        return false;
      }
    } else if (!getVersion().equals(other.getVersion())) {
      return false;
    }
    return true;
  }

  /**
   * @return the groupId
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * @return the artifactId
   */
  public String getArtifactId() {
    return artifactId;
  }

  /**
   * @return the version
   */
  public String getVersion() {
    return version;
  }

}