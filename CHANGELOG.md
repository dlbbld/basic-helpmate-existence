# Changelog

## [2.0.0] - KBN versus K theorem correction - 2026-10-06

This major release changes the theorem's meaning. Legal pawn promotion can produce
`KBNvK` positions with no helpmate even though Black is not forced to capture on
the first move. The broader `KBNvK` statement in version 1.x is false and is
superseded by the corrected statement in this release.

- Exclude the specified promotion traps: eight White-to-move and two Black-to-move
  placements, covering both bishop colours, with corresponding colour-reversed
  exceptions.
- Include straight and capture promotion in the sufficient illegality
  certificates, allow a king to move out of check, and check historical legality
  separately in every board orientation.
- Require every unwinnable `KBNvK` theorem root to be an explicit exception or have
  an illegality certificate.
- Add proof-game replay and independent forward searches using the ordinary move
  generator, including the supplied `g8=B+ Kh8` example and a cooperative mating
  continuation after the alternative `Kh6`.
- Distinguish ordinary legal-move reachability from actual play after a dead
  position has ended the game; the White-to-move `Nf5`/`Nc5` traces have an already
  dead predecessor.
- Record the completed isolated Syzygy position-set probes and remove the stale
  test-runtime estimate.

The existing statements for the other covered material classes retain their scope.

Validation: `mvn clean verify` on Java 17 passed all 32 tests and built the 2.0.0
package. The completed isolated probes cover
3,067,466 `KBNvK(light bishop)`, 2,827,104 `KRvKB(light bishop)`, and 2,915,128
`KRvKN` representatives with exact expected counts, as recorded in `tasks.md`.
