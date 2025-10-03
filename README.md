# `ol.sops`

> An extremely tiny and simple wrapper around the awesome sops (previously known as mozilla/sops)

[![Build Status](https://github.com/outskirtslabs/ol.sops/actions/workflows/ci.yml/badge.svg)](https://github.com/outskirtslabs/ol.sops/actions)
[![cljdoc badge](https://cljdoc.org/badge/com.outskirtslabs/ol.sops)](https://cljdoc.org/d/com.outskirtslabs/ol.sops)
[![Clojars Project](https://img.shields.io/clojars/v/com.outskirtslabs/ol.sops.svg)](https://clojars.org/com.outskirtslabs/ol.sops)

This is intended for [babashka][babashka] and JVM clojure and provides an idiomatic and data driven wrapper around the CLI tool.

## Installation

```clojure
{:deps {com.outskirtslabs/sops {:mvn/version "0.1.0"}}}

;; Leiningen
[com.outskirtslabs/sops "0.1.0"]
```

## Quick Start

```clojure
(ns myapp.core
  (:require [ol.sops :as sops]))

```

## Security

See [here][sec] for security advisories or to report a security vulnerability.

## License

Copyright © 2025 Casey Link <casey@outskirtslabs.com>

Distributed under the [MIT License](./LICENSE)

[sops]: https://github.com/getsops/sops
[sec]: https://github.com/outskirtslabs/ol.sops/security
[babashka]: https://babashka.org/
