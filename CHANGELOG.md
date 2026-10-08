# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- `PreviewCountryPicker` and `PreviewPhoneNumberPicker` are now private. They are Android Studio
  previews and were never meant to be part of the public API.
- The API documentation published with the library is now in Dokka's HTML format
- Published with
  the [Gradle Maven Publish Plugin](https://github.com/vanniktech/gradle-maven-publish-plugin)
  instead of the Sonatype Central Portal Publisher, which is no longer maintained. The library now
  also publishes Gradle Module Metadata, so Gradle resolves its dependencies more precisely.
- The build uses the new Android Gradle Plugin DSL, as the old publisher needed the legacy one

## [1.4.0] - 2026-09-25

### Added

- `PhoneNumberPicker`, a phone number field with the country's flag and calling code. Numbers are
  formatted and validated using each country's rules, and the country follows what is typed,
  e.g. typing a Toronto number after +1 selects Canada. International numbers can be typed,
  pasted or autofilled.
- `Country.callingCode`, `Country.dialCode` and `getAllPhoneCountries()`
- A `shape` option for `CountryPicker` and its search bar, e.g. `CircleShape` for fully rounded
- The country list can be searched by ISO 3166-1 alpha-3 code, e.g. "GBR", as well as alpha-2

### Changed

- Removed the dependency on `material-icons-core`. The few icons used are now part of the library.
- Added a dependency on Google's libphonenumber for phone number formatting and validation
- Upgraded to Kotlin 2.4.20, Compose BOM 2026.09.00 and Android Gradle Plugin 9.4.1

## [1.3.1] - 2024-09-18

### Changed

Removed Surface behind search bar so that it blends in.

## [1.3.0] - 2024-09-17

### Changed

Upgraded to the latest version of Compose, Kotlin etc.

## [1.2.0] - 2024-08-15

### Added

- Included the option to search the country list

### Fixed

- Corrected some erroneous translations of country in French and Chinese


## [1.1.0] - 2024-08-13

### Changed

- Updated some of the dependencies and used Kotlin 2.0

## [1.0.0] - 2024-03-12

### Added

- Initial version
