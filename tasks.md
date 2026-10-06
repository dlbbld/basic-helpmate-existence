# Completed verification tasks

All isolated Syzygy pointwise probes previously listed here are complete.

| Material class | Unique representatives | Result |
| --- | ---: | --- |
| `KRvK` | 50,015 | completed previously |
| `KQvK` | 46,137 | completed previously |
| `KBBvK(opposite bishops)` | 1,493,368 | completed previously |
| `KBNvK(light bishop)` | 3,067,466 | exact expected count |
| `KRvKB(light bishop)` | 2,827,104 | exact expected count |
| `KRvKN` | 2,915,128 | exact expected count |

The final three passes were reported in the verification run on 6 October 2026 for correction commit `b7e310b`. Each probe checked the generated material key and asserted the final representative count.

To rerun those probes, supply the local tablebase directory:

```sh
python -u scripts/verify_syzygy_position_sets.py --tablebase PATH_TO_SYZYGY_TABLES --isolate-table --progress 500000 --material "KBNvK(light bishop)"
python -u scripts/verify_syzygy_position_sets.py --tablebase PATH_TO_SYZYGY_TABLES --isolate-table --progress 500000 --material "KRvKB(light bishop)"
python -u scripts/verify_syzygy_position_sets.py --tablebase PATH_TO_SYZYGY_TABLES --isolate-table --progress 500000 --material KRvKN
```

## 2.0.0 release

- Scope: narrow the `KBNvK` theorem by explicitly excluding legal promotion traps,
  and correct the sufficient historical illegality certificates.
- GitHub release title: `2.0.0`.
- Release-notes heading: `KBN versus K theorem correction`, following the
  Ashlar Chess convention of a version-only GitHub title and a descriptive heading
  inside the release notes.
- Verification: Java 17 `mvn clean verify` passed all 32 tests with zero failures,
  errors, or skipped tests, and built the version 2.0.0 package.
- Publication: [GitHub release 2.0.0](https://github.com/dlbbld/basic-helpmate-existence/releases/tag/2.0.0),
  using the annotated `2.0.0` tag on the release pull request's merge commit.
