/**
 * Created by geddingsbarrineau on 5/1/16.
 */
$('#cont').before('<header class="main-header">'+'<!-- Logo -->'+
   ' <a href="index.html" class="logo">'+
      '<!-- mini logo for sidebar mini 50x50 pixels -->'+
      '<span class="logo-mini"><b>TANK</b>SDNos</span>'+
     ' <!-- logo for regular state and mobile devices -->'+
      '<span class="logo-lg"><b>TANK </b>SDNos</span>'+
    '</a>'+
   ' <!-- Header Navbar: style can be found in header.less -->'+
   ' <nav class="navbar navbar-static-top">'+
     ' <!-- Sidebar toggle button-->'+
      '<a href="#" class="sidebar-toggle" data-toggle="push-menu" role="button">'+
       ' <span class="sr-only">Toggle navigation</span>'+
      '</a>'+
      '<!-- Navbar Right Menu -->'+
      '<div class="navbar-custom-menu">'+
     ' </div>'+
'<div class="navbar-custom-menu">'+
        '<ul class="nav navbar-nav">'+
          '<!-- Messages: style can be found in dropdown.less-->'+
          '<li class="dropdown messages-menu">'+
            '<div  class="logo">'+
           		'<span id="home-button-title" class="logo-lg">192.168.1.1</span>'+
            '</a>'+
          '</li>'+
       ' </ul>'+
      '</div>'+
    '</nav>'+
  '</header>'+
'<aside class="main-sidebar">'+
    '<!-- sidebar: style can be found in sidebar.less -->'+
    '<section class="sidebar">'+
     ' <!-- search form -->'+
      '<form action="#" method="get" class="sidebar-form">'+
      '  <div class="input-group">'+
        '  <input type="text" name="q" class="form-control" placeholder="Search...">'+
          '<span class="input-group-btn">'+
            '    <button type="submit" name="search" id="search-btn" class="btn btn-flat">'+
              '    <i class="fa fa-search"></i>'+
             '   </button>'+
           '   </span>'+
       ' </div>'+
      '</form>'+
     ' <!-- /.search form -->'+
      '<!-- sidebar menu: : style can be found in sidebar.less -->'+
      '<ul class="sidebar-menu" data-widget="tree">'+
        '<li class="header">MAIN NAVIGATION</li>'+
       ' <li id="controller">'+
          '<a href="index.html">'+
            '<i class="fa fa-dashboard"></i> <span>Controller</span>'+
          '</a>'+
       ' </li>'+
       ' <li id="switches"  >'+
         ' <a href="switches.html">'+
           ' <i class="fa fa-exchange"></i> <span>Switches</span>'+
         ' </a>'+
        '</li>'+
        '<li id="hosts">'+
          '<a href="hosts.html">'+
           ' <i class="fa fa-desktop fa-fw"></i> <span>Hosts</span>   '+
         ' </a>'+
        '</li>'+
       ' <li id="links">'+
         ' <a href="links.html">'+
           ' <i class="fa fa-expand fa-fw"></i> <span>Links</span>'+
          '</a>'+
       ' </li>'+
       
 '<li id="firewall"><a href="firewall.html"><i class="fa fa-fire"></i> <span>Firewall</span></a></li>'+
        '<li  id="acl">'+
          '<a href="acl.html">'+
            '<i class="fa fa-warning fa-fw"></i> <span>Access Control Lists</span>'+
         ' </a>  '+
        '</li>'+
       ' <li id="monitor"><a href="monitor.html"><i class="fa fa-bar-chart-o fa-fw"></i> <span>Monitor</span></a></li>'+
      '   <li id="qos"><a href="qos.html"><i class="fa fa-sort-numeric-desc"></i> <span>QoS</span></a></li>'+
     ' </ul>'+
    '</section>'+
    '<!-- /.sidebar -->'+
  '</aside>');
/*
var ipaddress = $.cookie('cip');
if (ipaddress == null || ipaddress == "") window.location.href = "login.html";
var restport = $.cookie('cport');
if (restport == null || restport == "") window.location.href = "login.html";
*/

//document.getElementById("home-button-title").innerHTML = "SDNos For TANK - " + ipaddress + ":" + restport;
