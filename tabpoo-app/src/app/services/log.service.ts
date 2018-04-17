import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class LogService {

  private apiUrl:string = "http://192.168.1.101:4567";
  private token:string = "eyJraWQiOiJhOTA1ZGM1MmFmZDg2YzEwNDg5ZWRhZmRlMTA4NWU1M2Y4MWZmYmViIiwiYWxnIjoiUlMyNTYifQ==.eyJ1dWlkIjoiMTI1NGUxZmEtZTQxMC00ZDdjLThmNmQtNDBlMGQ2ZGVjMDVhIiwiaWF0IjoxNTIzOTI0MjUxLCJleHAiOjE1MjM5Mjc4NTF9.ff7vmJToInx_BSa6k975DE6EQhIH7DlMIDJANqgS3LBb0-BUL1Cv8XLMI66Qvj2cljKlkZw0Z76TmYg_hMUb5Q==";
  constructor(private http: HttpClient) { }

  saveLog(log:any) :Observable<boolean> {
    console.log(log);
    let url = this.apiUrl + '/poolog'
    let params: HttpParams = new HttpParams();
    const headers = new HttpHeaders().set('Authorization', this.token);// ${this.spotifyAuthorize.accessToken}`);
    return this.http.post<boolean>(url, log, {params, headers});
  }
}
