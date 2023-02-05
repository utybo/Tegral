import React from "react";
import clsx from "clsx";
import Layout from "@theme/Layout";
import CodeBlock from "@theme/CodeBlock";
import Link from "@docusaurus/Link";
import useDocusaurusContext from "@docusaurus/useDocusaurusContext";
import styles from "./index.module.css";
import HomepageFeatures from "@site/src/components/HomepageFeatures";
import version from "@site/src/version.js";

function HomepageHeader() {
  const { siteConfig } = useDocusaurusContext();
  return (
    <header className={clsx("hero hero--primary", styles.heroBanner)}>
      <div className={clsx("container", styles.container)}>
        <div className={clsx(styles.vertContainer)}>
          <div className={clsx(styles.introContainer)}>
            <h1 className="hero__title">
              <img src="/img/tegral_logo.svg" /> {siteConfig.title}{" "}
              <small>{version()}</small>
            </h1>
            <p className="hero__subtitle">{siteConfig.tagline}</p>
            {
              <div className={styles.buttons}>
                <Link
                  className="button button--secondary button--lg"
                  to="/docs/get-started"
                >
                  Get started
                </Link>
              </div>
            }
          </div>
          <div className={styles.codeBlockContainer}>
            <CodeBlock
              language="kotlin"
              title="App.kt"
              style={{ "flex-grow": "1" }}
            >
              {`class AppController : KtorController() {
  override fun Routing.install() {
    get("/hello") {
      call.respond("Hello World!")
    }
  }
}

fun main() {
  tegral {
    put(::AppController)
  }
}`}
            </CodeBlock>
          </div>
        </div>
      </div>
    </header>
  );
}

export default function Home() {
  const { siteConfig } = useDocusaurusContext();
  return (
    <Layout
      title={`Home`}
      description="Libraries for better Kotlin applications."
    >
      <HomepageHeader />
      <main>
        <HomepageFeatures />
      </main>
    </Layout>
  );
}
