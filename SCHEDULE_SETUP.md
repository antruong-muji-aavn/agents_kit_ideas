# Scheduling Daily Collection

The daily collection script should run automatically once per day.

## Option 1: Claude Code Scheduled Task (Recommended)

When the schedule skill is available, run:

```bash
/schedule create --name daily-collection --cron "0 9 * * *" --prompt "
cd /path/to/agents_kit_ideas
python3 scripts/daily_collection.py
git add reports/ .brain_local/
git commit -m 'chore: daily collection $(date +%Y-%m-%d)'
git push
"
```

This runs at 9 AM UTC daily.

## Option 2: Local cron (fallback)

Add to crontab:

```bash
crontab -e
```

Add line:

```
0 9 * * * cd /path/to/agents_kit_ideas && python3 scripts/daily_collection.py && git add reports/ .brain_local/ && git commit -m "chore: daily collection $(date +\%Y-\%m-\%d)" && git push
```

## Option 3: GitHub Actions (if repo on GitHub)

Create `.github/workflows/daily-collection.yml`:

```yaml
name: Daily Collection
on:
  schedule:
    - cron: '0 9 * * *'
  workflow_dispatch:

jobs:
  collect:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up Python
        uses: actions/setup-python@v4
        with:
          python-version: '3.10'

      - name: Run collection
        run: python3 scripts/daily_collection.py

      - name: Commit and push
        run: |
          git config user.name "Brain Collector"
          git config user.email "bot@agents-kit.local"
          git add reports/ .brain_local/
          git diff --quiet && git diff --staged --quiet || (
            git commit -m "chore: daily collection $(date +%Y-%m-%d)" &&
            git push
          )
```

## Manual run

To test or run manually:

```bash
python3 scripts/daily_collection.py --verbose
```

Outputs:
- Raw collections: `.brain_local/collections/raw_collection_<YYYY-MM-DD>.json`
- Summary: `reports/collection_<YYYY-MM-DD>.md`
