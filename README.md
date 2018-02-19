# PagerDuty team release their own free official plugin for jira, which support two-way synch. Search for it on Atlassian Marketplace

# pagerduty-jira
Repository contains few groovy scripts that allow us to create a ticket, set in progress or resolve ticket in jira via PagerDuty.

Requirements:
* Adaptavist ScriptRunner add-on for Jira
* Jira accessible to PagerDuty (at least 1 endpoint)
* PagerDuty

The main idea is to use Custom REST Endpoint in Jira with PagerDuty's WebHooks.
