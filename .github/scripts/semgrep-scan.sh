# baseline: PR's base commit or the branch's last commit before push
if [ "${EVENT_NAME}" = "pull_request" ]; 
then BASELINE_COMMIT_ID="${PR_BASE_COMMIT_ID}"
else BASELINE_COMMIT_ID="${COMMIT_BEFORE_PUSH_ID}"
fi

# first push of a new branch
if [ "${BASELINE_COMMIT_ID}" = "0000000000000000000000000000000000000000" ]; 
then BASELINE_COMMIT_ID="$(git merge-base origin/main HEAD)"
fi

# if there are findings absent from the baseline then fail (exit 1)
semgrep scan \
  --config r/java.lang.security \
  --baseline-commit "${BASELINE_COMMIT_ID}" \
  --error
