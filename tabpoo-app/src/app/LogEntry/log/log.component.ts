import { Component, OnInit } from '@angular/core';
import { LogService } from '../../services/log.service';

@Component({
  selector: 'app-log',
  templateUrl: './log.component.html',
  styleUrls: ['./log.component.css'],
  providers: [LogService]
})
export class LogComponent implements OnInit {

  bristolSelection: number = null;

  constructor(private logService: LogService) { }

  ngOnInit() {
  }

  onSelection(value: number) {
    console.log(value);
    this.bristolSelection = value;
  }

  onSave() {
    console.log("save Now " + this.bristolSelection);
    let timestamp = Math.floor(new Date().getTime() / 1000);
    this.logService.saveLog({timestamp: timestamp, bristolLevel: this.bristolSelection})
      .subscribe((success) => console.log(success));
  }

}
