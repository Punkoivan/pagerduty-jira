import groovyx.net.http.HTTPBuilder
import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.*
import groovy.json.JsonSlurper

import org.apache.log4j.Logger
import org.apache.log4j.Level


class Pager {
    String incident_id
    String token
    String from

    Logger logger = Logger.getLogger("")

    def get_issueid() {
    '''
    Method send a get request to PD and fetch first note, which
    contains link to jira issue:
    https://jira.base.url.com/browse/PROJECTKEY-1
    and get an ssue id. We need to know this id
    when need to update ticket - set status to "Resolved"
    or "In Progress".
    '''
        def http = new HTTPBuilder("https://api.pagerduty.com/incidents/${this.incident_id}/notes", JSON )
        http.setHeaders('Authorization': "Token token=${this.token}", 'Content-Type': "applicat0ion/json",
                         'Accept': "application/vnd.pagerduty+json;version=2",
                         'From': "${this.from}")
        http.get( requestContentType: JSON ) { resp, json ->
            def jira_url = json['notes']['content'][0].toString()
            return jira_url.tokenize('/')[-1]
            }
    }

    def get_notes() {
    '''
    Method send a get request to PD and
    fetch all notes.
    Return: map with notes.
    '''
        this.logger.setLevel(Level.DEBUG)
        def http = new HTTPBuilder("https://api.pagerduty.com/incidents/${this.incident_id}/notes", JSON )
        http.setHeaders('Authorization': "Token token=${this.token}", 'Content-Type': "application/json",
                         'Accept': "application/vnd.pagerduty+json;version=2",
                         'From': "${this.from}")
        http.get( requestContentType: JSON ) { resp, json ->
            def notes = json['notes']['content'].toString()
            }
        return notes
    }

    def create_note(String text) {
    '''
    Method send a post request to PD
    and create a note with %text.
    Jira send a note if any update
    on Jira side (as postfunction script)
    '''
        def http = new HTTPBuilder("https://api.pagerduty.com/incidents/${this.incident_id}/notes", JSON )
        def data = [note: [content: text ]]
        http.setHeaders('Authorization': "Token token=${this.token}", 'Content-Type': "application/json",
                         'Accept': "application/vnd.pagerduty+json;version=2",
                         'From': "${this.from}")
        http.post( body: data,
                   requestContentType: JSON ) { resp ->
        }
    }

    def updateInc(String state) {
    '''
    Method send a put request to PD
    and update an incident to %state.
    Jira changes incident status 
    to "Resolved"
    if incident has been resolved
    in Jira or to "Acknowledged" in
    case of work in progress
    (realized via postfunction)
    I had to use RESTClient for put  
    request - couldn't make 
    withHTTPBuilder.
    '''
        this.logger.setLevel(Level.DEBUG)
        def client = new RESTClient('https://api.pagerduty.com')
        def jsonObj = new JsonSlurper().parseText("{\"incidents\": [{\"id\": \"${this.incident_id}\", \"type\": \"incident_reference\", \"status\": \"${state}\"}]")

        try {
            def response = client.put( path: '/incidents',
                contentType: JSON,
                headers: ['Authorization': "Token token=${this.token}", 'Content-Type': "application/json",
                            'Accept': "application/vnd.pagerduty+json;version=2",
                            'From': "${this.from}"],
                body: jsonObj)
            }
        catch( ex ) {
                this.logger.debug(ex.response.getData())
        }
    }

    def get_type(String pd_hook) {
    /*
    Receive PagerDuty hook as an argument, parse data
    return message type (triggered, annotate, resolved, acknowledged).
    */
    def jsonSlurper = new JsonSlurper()
    def hook_json = jsonSlurper.parseText(pd_hook)
    def type = hook_json.messages.event[0]
    return type
    }
}

