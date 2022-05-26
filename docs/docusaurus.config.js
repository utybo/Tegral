// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Tegral',
  tagline: 'Libraries for better Kotlin applications.',
  url: 'https://your-docusaurus-test-site.com', // TODO
  baseUrl: '/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'throw',
  favicon: 'img/tegral_logo.png', // TODO replace with something else

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'utybo', // Usually your GitHub org/user name.
  projectName: 'tegral', // Usually your repo name.

  // Even if you don't use internalization, you can use this field to set useful
  // metadata like html lang. For example, if your site is Chinese, you may want
  // to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          editUrl:
            'https://github.com/facebook/docusaurus/tree/main/packages/create-docusaurus/templates/shared/', // TODO update to proper URL
        },
        blog: {
          showReadingTime: true,
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          editUrl:
            'https://github.com/facebook/docusaurus/tree/main/packages/create-docusaurus/templates/shared/', // TODO update to proper URL
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      navbar: {
        title: 'Tegral',
        logo: {
          alt: 'Tegral Logo',
          src: 'img/tegral_logo.svg',
        },
        items: [
          {
            type: 'doc',
            docId: 'core/index',
            position: 'left',
            label: 'Core',
          },
          {
            type: 'doc',
            docId: 'web/index',
            position: 'left',
            label: 'Web'
          },
          {
            href: 'pathname:///api',
            label: 'API',
            position: 'right'
          },
          // {to: '/blog', label: 'Blog', position: 'left'}, // TODO
          {
            href: 'https://github.com/utybo/tegral',
            label: 'GitHub',
            position: 'right'
          }
        ],
      },
      footer: {
        style: 'dark',
        links: [
          // TODO
          {
            title: 'Docs',
            items: [
              /*{
                label: 'Tutorial',
                to: '/docs/intro',
              },*/
            ],
          },
          // TODO
          {
            title: 'Community',
            items: [
              {
                label: 'Stack Overflow',
                href: 'https://stackoverflow.com/questions/tagged/docusaurus',
              },
              {
                label: 'Discord',
                href: 'https://discordapp.com/invite/docusaurus',
              },
              {
                label: 'Twitter',
                href: 'https://twitter.com/docusaurus',
              },
            ],
          },
          // TODO
          {
            title: 'More',
            items: [
              /*{ // TODO
                label: 'Blog',
                to: '/blog',
              },*/
              {
                label: 'GitHub',
                href: 'https://github.com/facebook/docusaurus',
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} Tegral maintainers & contributors. Built with Docusaurus.`,
      },
      prism: {
        theme: lightCodeTheme,
        darkTheme: darkCodeTheme,
        additionalLanguages: ['kotlin', 'groovy'],
      },
    }),
};

module.exports = config;