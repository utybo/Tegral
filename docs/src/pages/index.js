import React from "react";
import clsx from "clsx";
import Layout from "@theme/Layout";
import CodeBlock from "@theme/CodeBlock";
import Link from "@docusaurus/Link";
import useDocusaurusContext from "@docusaurus/useDocusaurusContext";
import styles from "./index.module.css";
import version from "@site/src/version.js";
import tegralDiExample from "!!raw-loader!./tegral-di-example.kt";
import tegralOpenApiExample from "!!raw-loader!./tegral-openapi-example.kt";
import tegralWebExample from "!!raw-loader!./tegral-web-example.kt";
import tegralNiwenExample from "!!raw-loader!./tegral-niwen-example.kt";
import tegralPrismaKtExample from "!!raw-loader!./tegral-prismakt-example.kt";

// TODO liens fonctionnels
// TODO svg minifiés

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

const tegralDiCard = () => (
  <div className={clsx(styles.libsCard, "card")}>
    <div className={clsx(styles.cardContent)}>
      <div className="card__header">
        <h3>
          <img src="/img/tegral_logo_v2_di.svg" height="36" />
          <span className={styles.cardTitle}>Tegral DI</span>
        </h3>
      </div>
      <div className="card__body">
        <p>
          A powerful and testable dependency injection framework for building
          all kinds of apps.
        </p>
        <div className={styles.cardLinks}>
          <Link className="button button--outline button--secondary" to="/docs/modules/core/di/introduction-to-di">
            Introduction
          </Link>
          <Link className="button button--outline button--primary" to="docs/modules/core/di">
            Documentation
          </Link>
        </div>
      </div>
    </div>
    <div className={clsx(styles.cardExample)}>
      <CodeBlock language="kotlin">{tegralDiExample}</CodeBlock>
    </div>
  </div>
);


const tegralOpenApiCard = () => (
  <div className={clsx(styles.libsCard, "card")}>
    <div className={clsx(styles.cardContent)}>
      <div className="card__header">
        <h3>
          <img src="/img/tegral_logo_v2_openapi.svg" height="36" />
          <span className={styles.cardTitle}>Tegral OpenAPI</span>
        </h3>
      </div>
      <div className="card__body">
        <p>
          Create OpenAPI specs in Kotlin and serve Swagger UI. Can be used stand-alone, with Ktor or with Tegral Web.
        </p>
        <div className={styles.cardLinks}>
          <Link className="button button--outline button--secondary" to="/docs/modules/core/openapi/scripting">
            Scripts
          </Link>
          <Link className="button button--outline button--secondary" to="/docs/modules/core/openapi/ktor">
            For Ktor
          </Link>
          <Link className="button button--outline button--secondary" to="/docs/modules/core/openapi/tegral-web">
            For Tegral Web
          </Link>
          <Link className="button button--outline button--primary" to="/docs/modules/core/openapi">
            Documentation
          </Link>
        </div>
      </div>
    </div>
    <div className={clsx(styles.cardExample)}>
      <CodeBlock language="kotlin">{tegralOpenApiExample}</CodeBlock>
    </div>
  </div>
);

const tegralWebCard = () => (
  <div className={clsx(styles.libsCard, "card")}>
    <div className={clsx(styles.cardContent)}>
      <div className="card__header">
        <h3>
          <img src="/img/tegral_logo_v2_web.svg" height="36" />
          <span className={styles.cardTitle}>Tegral Web</span>
        </h3>
      </div>
      <div className="card__body">
        <p>
          The Kotlin-est way to build back-end applications. Make use of the entire Tegral stack in a simple, cohesive experience.
        </p>
        <div className={styles.cardLinks}>
          <Link className="button button--outline button--secondary" to="/docs/get-started/">
            Tutorial series
          </Link>
          <Link className="button button--outline button--primary" to="/docs/modules/web">
            Documentation
          </Link>
        </div>
      </div>
    </div>
    <div className={clsx(styles.cardExample)}>
      <CodeBlock language="kotlin">{tegralWebExample}</CodeBlock>
    </div>
  </div>
);

const tegralNiwenCard = () => (
  <div className={clsx(styles.libsCard, "card")}>
  <div className={clsx(styles.cardContent)}>
    <div className="card__header">
      <h3>
        <img src="/img/tegral_logo_v2_niwen.svg" height="36" />
        <span className={styles.cardTitle}>Tegral Niwen</span>
      </h3>
    </div>
    <div className="card__body">
      <p>
        Create simple lexers and parsers with Tegral Niwen's handy DSL.
        A good fit for prototyping, toy projects or simple languages.
      </p>
      <div className={styles.cardLinks}>
        <Link className="button button--outline button--secondary" to="/docs/modules/core/niwen/lexer">
          Lexer
        </Link>
        <Link className="button button--outline button--secondary" to="/docs/modules/core/niwen/parser">
          Parser
        </Link>
        <Link className="button button--outline button--primary" to="/docs/modules/core/niwen">
          Documentation
        </Link>
      </div>
    </div>
  </div>
  <div className={clsx(styles.cardExample)}>
    <CodeBlock language="kotlin">{tegralNiwenExample}</CodeBlock>
  </div>
</div>
)


const tegralPrismaktCard = () => (
  <div className={clsx(styles.libsCard, "card")}>
  <div className={clsx(styles.cardContent)}>
    <div className="card__header">
      <h3>
        <img src="/img/tegral_logo_v2_prismakt.png" height="36" />
        <span className={styles.cardTitle}>Tegral PrismaKT</span>
        <span class="badge badge--secondary">Experimental</span>
      </h3>
    </div>
    <div className="card__body">
      <p>
        Use <a href="https://prisma.io">Prisma</a> in your Kotlin applications. Generate JetBrains Exposed bindings for your databases for an awesome Prisma + Kotlin experience.
      </p>
      <div className={styles.cardLinks}>
        <Link className="button button--outline button--primary" to="/docs/modules/core/prismakt">
          Documentation
        </Link>
      </div>
    </div>
  </div>
  <div className={clsx(styles.cardExample)}>
    <CodeBlock language="kotlin">{tegralPrismaKtExample}</CodeBlock>
  </div>
</div>
)

export default function Home() {
  const { siteConfig } = useDocusaurusContext();
  return (
    <Layout
      title={`Home`}
      description="Libraries for better Kotlin applications."
    >
      <HomepageHeader />
      <main>
        <div className={styles.libs}>
          <h2>Tegral libraries</h2>
          <p className={styles.libsExplainer}>
            Integrable, reusable and documented. Everything you need to perfect
            your Kotlin app foundations.
          </p>
          <div className={styles.cardsContainer}>
            {tegralDiCard()}
            {tegralOpenApiCard()}
            {tegralWebCard()}
            {tegralNiwenCard()}
            {tegralPrismaktCard()}
          </div>
        </div>
        {/*<HomepageFeatures />*/}
      </main>
    </Layout>
  );
}
