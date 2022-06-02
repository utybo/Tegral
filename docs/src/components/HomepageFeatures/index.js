import React from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';

const FeatureList = [
  {
    title: 'Focus on your app',
    Svg: require('@site/static/img/undraw_developer_activity.svg').default,
    description: (
      <>
        Tegral provides all the fundamentals, leaving you free to build whatever
        you want. You no longer need to spend hours looking for libraries to
        build your own Kotlin-centric back-end stack.
      </>
    ),
  },
  {
    title: 'Integrable by design',
    Svg: require('@site/static/img/undraw_good_team.svg').default,
    description: (
      <>
        Tegral is designed from the ground up for interoperability. You can take
        just a handful of libraries, or the entire framework. We want to make
        your life easier, not lock you into an ecosystem.
      </>
    ),
  },
  {
    title: 'Built on Open-Source',
    Svg: require('@site/static/img/undraw_product_teardown.svg').default,
    description: (
      <>
        Tegral is built on top of awesome open-source libraries like Ktor,
        Exposed, and more. It's everything you love about these libraries, even
        more integrated and painless!
      </>
    ),
  },
  {
    title: 'Did someone say \'tests\'?',
    Svg: require('@site/static/img/undraw_science.svg').default,
    description: (
      <>
        Easy unit tests? Check. Easy integration and end-to-end tests? Check.
        Testing is not an afterthought. Tegral is built with tests in mind.
      </>
    )
  },
  {
    title: 'No magic.',
    Svg: require('@site/static/img/undraw_random_thoughts.svg').default,
    description: (
      <>
        Tegral comes with zero code generation or magic symbol processing. This
        keeps your build process simple and straightforward.
      </>
    )
  },
  {
    title: 'Powered by Kotlin',
    Svg: require('@site/static/img/undraw_kotlin_life.svg').default,
    description: (
      <>
        Tegral is built on top of Kotlin, a modern programming language that
        makes creating all kinds of apps easy and fun.
      </>
    )
  }
];

function Feature({Svg, title, description}) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
