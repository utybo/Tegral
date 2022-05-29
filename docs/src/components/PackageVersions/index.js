import React from 'react';
import Admonition from '@theme/Admonition'

export default function PackageVersions({ libraries }) {
  const libRows = libraries.map(lib => (
    <tr>
      <td><code>{lib.name}</code></td>
      <td><code>tegralLibs.{lib.catalog}</code></td>
      <td><code>guru.zoroark.tegral:{lib.name}:VERSION</code></td>
    </tr>
  ))
  return (
    <Admonition type="note" title="Package information">
      <table>
          <thead>
            <tr>
              <th align="center">Package name</th>
              <th><a href="/core/catalog/index.md">Catalog</a> dependency</th>
              <th>Full Gradle name</th>
            </tr>
          </thead>
          <tbody>
            {libRows}
          </tbody>
        </table>
    </Admonition>
  )
}

