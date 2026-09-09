# is-a.dev registration preparation — finlux.is-a.dev

Target domain:

```text
finlux.is-a.dev
```

Hosting target:

```text
GitHub Pages
https://khoaiprovip123.github.io/FinLux/
```

DNS record to submit to `is-a-dev/register`:

```json
{
  "owner": {
    "username": "khoaiprovip123",
    "email": "vankhoai690@gmail.com"
  },
  "records": {
    "CNAME": "khoaiprovip123.github.io"
  }
}
```

## Current status

- [x] `finlux.json` is currently unclaimed in `is-a-dev/register`.
- [x] Owner username: `khoaiprovip123`.
- [x] Contact email is present.
- [x] FinLux is a software-development-related open-source project.
- [x] Current website content is non-commercial.
- [ ] GitHub Pages must be enabled so the preview URL is reachable.
- [ ] Owner must personally confirm acceptance of the is-a.dev Terms of Service.
- [ ] Fork `is-a-dev/register` into `khoaiprovip123/register`.
- [ ] Copy `finlux.json` to `domains/finlux.json` in the fork.
- [ ] Use the upstream PR template in `PR_BODY.md` after the preview URL is live.
- [ ] Submit PR to `is-a-dev/register:main`.
- [ ] After merge, configure GitHub Pages custom domain as `finlux.is-a.dev`.
- [ ] Enable HTTPS after DNS propagation.

## Important

Do not add the GitHub Pages custom domain before the is-a.dev PR is merged. The official is-a.dev GitHub Pages guide recommends configuring the custom domain after DNS is published.
