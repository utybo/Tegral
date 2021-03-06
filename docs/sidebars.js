/**
 * Creating a sidebar enables you to:
 - create an ordered group of docs
 - render a sidebar for each doc of that group
 - provide next/previous navigation

 The sidebars can be generated from the filesystem, or explicitly defined here.

 Create as many sidebars as you want.
 */

// @ts-check

/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
  'get-started': [
    'get-started/index',
    'get-started/tutorial/step-1-hello-world',
    'get-started/tutorial/step-2-adding-tests'
  ],
  core: [
    'core/index',
    'core/design',
    'core/catalog/index',
    {
      type: 'category',
      label: 'Tegral DI',
      link: { type: 'doc', id: 'core/di/index' },
      items: [
        'core/di/getting-started',
        'core/di/introduction-to-di',
        'core/di/injection',
        'core/di/environment',
        'core/di/qualifiers',
        'core/di/modules',
        'core/di/aliases',
        'core/di/internals',
        {
          type: 'category',
          label: 'Testing',
          link: { type: 'doc', id: 'core/di/testing/index' },
          items: [
            'core/di/testing/writing-tests',
            'core/di/testing/checks'
          ]
        },
        {
          type: 'category',
          label: 'Extensions',
          link: { type: 'doc', id: 'core/di/extensions/introduction' },
          items: [
            'core/di/extensions/factories',
            'core/di/extensions/services'
          ]
        }
      ]
    },
    {
      type: 'category',
      label: 'Tegral Config',
      link: { type: 'doc', id: 'core/config/index' },
      items: [
        'core/config/sections'
      ]
    },
    'core/featureful/index',
    'core/services/index',
    'core/logging/index'
  ],
  web: [
    'web/index',
    'web/appdsl/index',
    'web/appdefaults/index',
    'web/controllers/index',
    'web/config/index',
    'web/apptest/index'
  ],
  about: [
    'about/contributing',
    'about/security'
  ]
};

module.exports = sidebars;
