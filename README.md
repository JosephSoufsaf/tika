# GitHub Actions Workflow Modification Design and Implementation Choices

The goal is to have the build fail when a commit introduces a new vulnerability detected by Semgrep, without failing for the findings that already exist in Tika.

## Rules and Findings

In the original `tika-core` code, Semgrep already reported 8 findings:
- [semgrep-before.txt](https://github.com/JosephSoufsaf/tika/blob/tache3/semgrep-before/semgrep-before.txt)
- [semgrep-before.json](https://github.com/JosephSoufsaf/tika/blob/tache3/semgrep-before/semgrep-before.json)

These files were obtained by running `semgrep scan --config r/java.lang.security tika-core --[text/json] --output semgrep-avant.[txt/json]` in Powershell locally as documented Semgrep's CLI [reference page](https://semgrep.dev/docs/cli-reference). To choose the rules, we filtered Semgrep's rule registry ([semgrep.dev/r](https://semgrep.dev/r)) for `java.lang.security`. Here, `java` is the language, `lang` designates rules for the standard language and its library, and `security` is the security category. The search returned 115 rules. Of these, 29 are Pro rules, which can only be run with a Semgrep account, and 4 are not associated to Java code. The workflow therefore runs the remaining 82 rules applicable to Java.

Only new findings, not including the 8 above, should be checked for regression. To do so, we modified the case study's CI workflow that builds and tests `tika-core` (`tache2-tests.yml`, added in Tâche 2) and renamed it [`cas-etude-workflow.yml`](.github/workflows/cas-etude-workflow.yml). As before for Tâche 2, Tika's original workflows are disabled.

The workflow runs on every push, every pull request, and manually. It has two parallel jobs: `semgrep`, which looks for new vulnerabilities, and `build and test tika-core`. If either fails, the run fails. Keeping them separate shows directly why a build failed, and lets the tests run even when a vulnerability is found.

## `semgrep` job in workflow

The job checks out the code with its full history (needed to access the baseline commit), installs Python and Semgrep 1.183.0 (the same version as used locally), then runs the script [`semgrep-scan.sh`](https://github.com/JosephSoufsaf/tika/blob/tache3/.github/scripts/semgrep-scan.sh).

With the `--baseline-commit` option in the [Semgrep CLI Reference](https://semgrep.dev/docs/cli-reference), Semgrep only analyzes the files changed since a reference commit and only reports findings absent from that commit. The existing findings are therefore ignored. The `--error` option leads to exit with code 1 if there are any new findings.

On a push, the baseline is the branch's last commit before the push, so the failure points to the exact commit that introduced the vulnerability. On a pull request, it's the PR's base commit, so all of the PR's commits are checked, not just the last one. On a manual run or the first push of a new branch, GitHub provides no previous commit, so the script uses the point where the branch left `main`.

# Documentation of Vulnerabilities


